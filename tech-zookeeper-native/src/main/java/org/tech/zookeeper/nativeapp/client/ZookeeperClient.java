package org.tech.zookeeper.nativeapp.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Zookeeper原生客户端封装类
 * 提供Zookeeper基本操作的封装，包括节点的CRUD、监听等功能
 */
@Slf4j
@Component
public class ZookeeperClient {

    @Autowired
    private org.tech.zookeeper.nativeapp.config.ZookeeperConfig config;

    /**
     * Zookeeper原生客户端实例
     */
    private ZooKeeper zooKeeper;

    /**
     * 初始化Zookeeper连接
     * 使用CountDownLatch等待连接建立完成
     */
    @PostConstruct
    public void init() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(
                    config.getConnectString(),
                    config.getSessionTimeout(),
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                                latch.countDown();
                                log.info("Zookeeper连接成功");
                            }
                        }
                    }
            );
            latch.await();
        } catch (Exception e) {
            log.error("Zookeeper连接失败", e);
            throw new RuntimeException("Zookeeper连接失败", e);
        }
    }

    /**
     * 关闭Zookeeper连接
     * 在Spring容器销毁时自动调用
     */
    @PreDestroy
    public void destroy() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
                log.info("Zookeeper连接已关闭");
            } catch (InterruptedException e) {
                log.error("关闭Zookeeper连接失败", e);
            }
        }
    }

    /**
     * 创建持久节点
     * 持久节点在创建后会一直存在，直到被显式删除
     *
     * @param path 节点路径，如 /demo/node1
     * @param data 节点数据，字节数组形式
     * @return 创建成功的节点路径
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public String createPersistentNode(String path, byte[] data) throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * 创建临时节点
     * 临时节点在客户端会话结束后自动删除
     *
     * @param path 节点路径，如 /demo/temp1
     * @param data 节点数据，字节数组形式
     * @return 创建成功的节点路径
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public String createEphemeralNode(String path, byte[] data) throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    /**
     * 创建带序号的持久节点
     * 在路径后自动追加递增序号，如 /demo/seq-0000000001
     *
     * @param path 节点路径前缀，如 /demo/seq-
     * @param data 节点数据，字节数组形式
     * @return 创建成功的节点路径（包含序号）
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public String createPersistentSequentialNode(String path, byte[] data) throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * 创建带序号的临时节点
     * 在路径后自动追加递增序号，客户端会话结束后自动删除
     * 常用于实现分布式锁
     *
     * @param path 节点路径前缀，如 /demo/lock-
     * @param data 节点数据，字节数组形式
     * @return 创建成功的节点路径（包含序号）
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public String createEphemeralSequentialNode(String path, byte[] data) throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * 获取节点数据
     *
     * @param path 节点路径
     * @return 节点数据，字节数组形式
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public byte[] getData(String path) throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, false, null);
    }

    /**
     * 获取节点数据（带监听）
     * 当节点数据发生变化时，会触发Watcher回调
     *
     * @param path    节点路径
     * @param watcher 监听器，用于处理节点变化事件
     * @return 节点数据，字节数组形式
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public byte[] getDataWithWatcher(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watcher, null);
    }

    /**
     * 设置节点数据
     *
     * @param path    节点路径
     * @param data    新的节点数据，字节数组形式
     * @param version 版本号，-1表示不检查版本直接更新
     * @return 节点状态信息
     * @throws KeeperException Zookeeper异常（版本不匹配时抛出BadVersionException）
     * @throws InterruptedException 中断异常
     */
    public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
        return zooKeeper.setData(path, data, version);
    }

    /**
     * 删除节点
     *
     * @param path    节点路径
     * @param version 版本号，-1表示不检查版本直接删除
     * @throws KeeperException Zookeeper异常（节点不存在或版本不匹配时抛出异常）
     * @throws InterruptedException 中断异常
     */
    public void deleteNode(String path, int version) throws KeeperException, InterruptedException {
        zooKeeper.delete(path, version);
    }

    /**
     * 检查节点是否存在
     *
     * @param path 节点路径
     * @return 节点状态信息，如果节点不存在返回null
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public Stat exists(String path) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, false);
    }

    /**
     * 检查节点是否存在（带监听）
     * 当节点被创建或删除时，会触发Watcher回调
     *
     * @param path    节点路径
     * @param watcher 监听器，用于处理节点变化事件
     * @return 节点状态信息，如果节点不存在返回null
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public Stat existsWithWatcher(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, watcher);
    }

    /**
     * 获取子节点列表
     *
     * @param path 父节点路径
     * @return 子节点名称列表
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, false);
    }

    /**
     * 获取子节点列表（带监听）
     * 当子节点列表发生变化时，会触发Watcher回调
     *
     * @param path    父节点路径
     * @param watcher 监听器，用于处理子节点变化事件
     * @return 子节点名称列表
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    public List<String> getChildrenWithWatcher(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watcher);
    }

    /**
     * 获取Zookeeper原生客户端实例
     * 用于需要直接操作原生API的场景
     *
     * @return ZooKeeper原生客户端实例
     */
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
}
