package org.tech.zookeeper.nativeapp.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于Zookeeper实现的分布式锁
 * 使用临时有序节点实现公平锁，避免惊群效应
 * 每个线程在获取锁时创建一个临时有序节点，然后监听前一个节点的删除事件
 * 当前一个节点删除时，当前线程获取锁
 */
@Slf4j
@Component
public class DistributedLock implements Lock {

    @Autowired
    private ZookeeperClient zookeeperClient;

    /**
     * 锁的路径前缀
     */
    private String lockPath;

    /**
     * 当前线程创建的节点路径
     */
    private String currentNodePath;

    /**
     * 存储当前线程持有的节点路径，支持多线程环境
     */
    private ThreadLocal<String> currentThreadPath = new ThreadLocal<>();

    /**
     * 分布式锁的根节点路径
     */
    private static final String LOCK_ROOT = "/tech-zookeeper-locks";

    /**
     * 默认构造函数
     */
    public DistributedLock() {
    }

    /**
     * 带锁路径参数的构造函数
     *
     * @param lockPath 锁名称，会自动添加到LOCK_ROOT下
     */
    public DistributedLock(String lockPath) {
        this.lockPath = LOCK_ROOT + "/" + lockPath;
    }

    /**
     * 设置锁路径
     *
     * @param lockPath 锁名称
     */
    public void setLockPath(String lockPath) {
        this.lockPath = LOCK_ROOT + "/" + lockPath;
    }

    /**
     * 获取锁（阻塞式）
     * 如果获取不到锁，会阻塞等待直到获取成功
     */
    @Override
    public void lock() {
        try {
            if (!tryLock()) {
                waitForLock();
            }
        } catch (Exception e) {
            throw new RuntimeException("获取锁失败", e);
        }
    }

    /**
     * 尝试获取锁（非阻塞式）
     * 创建临时有序节点，判断自己是否为最小序号节点
     * 如果是，获取锁成功；否则，获取锁失败
     *
     * @return true表示获取锁成功，false表示获取锁失败
     */
    @Override
    public boolean tryLock() {
        try {
            ZooKeeper zooKeeper = zookeeperClient.getZooKeeper();

            // 确保锁根节点存在
            Stat stat = zooKeeper.exists(LOCK_ROOT, false);
            if (stat == null) {
                zooKeeper.create(LOCK_ROOT, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // 创建临时有序节点
            currentNodePath = zooKeeper.create(lockPath + "-", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            currentThreadPath.set(currentNodePath);

            // 获取所有子节点并排序
            List<String> children = zooKeeper.getChildren(LOCK_ROOT, false);
            Collections.sort(children);

            // 判断当前节点是否为最小序号节点
            String currentNode = currentNodePath.substring(LOCK_ROOT.length() + 1);
            if (children.get(0).equals(currentNode)) {
                log.info("线程{}获取锁成功，节点:{}", Thread.currentThread().getName(), currentNodePath);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("尝试获取锁失败", e);
            return false;
        }
    }

    /**
     * 在指定时间内尝试获取锁
     * 如果在超时时间内获取到锁，返回true；否则返回false
     *
     * @param time 等待时间
     * @param unit 时间单位
     * @return true表示获取锁成功，false表示超时
     * @throws InterruptedException 中断异常
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long start = System.currentTimeMillis();
        long timeout = unit.toMillis(time);

        while (System.currentTimeMillis() - start < timeout) {
            if (tryLock()) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }

    /**
     * 等待锁（阻塞式）
     * 找到当前节点的前一个节点，监听其删除事件
     * 当前一个节点删除时，唤醒等待的线程
     *
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    private void waitForLock() throws KeeperException, InterruptedException {
        ZooKeeper zooKeeper = zookeeperClient.getZooKeeper();
        String currentNode = currentNodePath.substring(LOCK_ROOT.length() + 1);

        // 获取所有子节点并排序
        List<String> children = zooKeeper.getChildren(LOCK_ROOT, false);
        Collections.sort(children);

        int index = children.indexOf(currentNode);
        if (index == 0) {
            // 如果当前节点是最小序号节点，直接获取锁
            log.info("线程{}获取锁成功，节点:{}", Thread.currentThread().getName(), currentNodePath);
            return;
        }

        // 获取前一个节点
        String predecessor = children.get(index - 1);
        String predecessorPath = LOCK_ROOT + "/" + predecessor;

        // 创建CountDownLatch等待前一个节点删除
        CountDownLatch latch = new CountDownLatch(1);
        Watcher watcher = event -> {
            // 监听前一个节点的删除事件
            if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                latch.countDown();
            }
        };

        // 为前一个节点注册监听器
        Stat stat = zooKeeper.exists(predecessorPath, watcher);
        if (stat != null) {
            log.info("线程{}等待前一个节点:{}释放锁", Thread.currentThread().getName(), predecessorPath);
            latch.await();
        }
    }

    /**
     * 释放锁
     * 删除当前线程创建的临时节点
     */
    @Override
    public void unlock() {
        try {
            String path = currentThreadPath.get();
            if (path != null) {
                zookeeperClient.getZooKeeper().delete(path, -1);
                log.info("线程{}释放锁，节点:{}", Thread.currentThread().getName(), path);
                currentThreadPath.remove();
            }
        } catch (Exception e) {
            log.error("释放锁失败", e);
        }
    }

    /**
     * 创建Condition对象
     * 本实现不支持Condition
     *
     * @throws UnsupportedOperationException 不支持此操作
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("不支持Condition");
    }

    /**
     * 获取可中断锁
     * 本实现不支持可中断锁
     *
     * @throws InterruptedException 中断异常
     * @throws UnsupportedOperationException 不支持此操作
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("不支持可中断锁");
    }
}
