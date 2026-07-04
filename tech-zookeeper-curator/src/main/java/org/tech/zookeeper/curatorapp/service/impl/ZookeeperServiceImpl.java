package org.tech.zookeeper.curatorapp.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tech.zookeeper.curatorapp.service.ZookeeperService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Zookeeper服务实现类（Curator客户端）
 * 封装Curator客户端的操作，提供业务层调用接口
 * 包含节点CRUD、分布式锁、Leader选举、分布式计数器、监听器等功能
 */
@Slf4j
@Service
public class ZookeeperServiceImpl implements ZookeeperService {

    @Autowired
    private CuratorFramework curatorFramework;

    /**
     * 分布式锁缓存，key为锁名称，value为InterProcessMutex实例
     */
    private final Map<String, InterProcessMutex> lockMap = new ConcurrentHashMap<>();

    /**
     * Leader选举器缓存，key为选举路径，value为LeaderSelector实例
     */
    private final Map<String, LeaderSelector> leaderSelectorMap = new ConcurrentHashMap<>();

    /**
     * 分布式计数器缓存，key为计数器路径，value为SharedCount实例
     */
    private final Map<String, SharedCount> counterMap = new ConcurrentHashMap<>();

    /**
     * CuratorCache监听器缓存，key为监听路径，value为CuratorCache实例
     */
    private final Map<String, CuratorCache> cacheMap = new ConcurrentHashMap<>();

    /**
     * 当前节点是否为Leader的标识
     */
    private final AtomicBoolean isLeader = new AtomicBoolean(false);

    /**
     * 创建持久节点
     *
     * @param path 节点路径
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径
     * @throws Exception Zookeeper异常
     */
    @Override
    public String createPersistentNode(String path, String data) throws Exception {
        String result = curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建持久节点成功: {}", result);
        return result;
    }

    /**
     * 创建临时节点
     *
     * @param path 节点路径
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径
     * @throws Exception Zookeeper异常
     */
    @Override
    public String createEphemeralNode(String path, String data) throws Exception {
        String result = curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建临时节点成功: {}", result);
        return result;
    }

    /**
     * 创建带序号的持久节点
     *
     * @param path 节点路径前缀
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径（包含序号）
     * @throws Exception Zookeeper异常
     */
    @Override
    public String createPersistentSequentialNode(String path, String data) throws Exception {
        String result = curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建带序号持久节点成功: {}", result);
        return result;
    }

    /**
     * 创建带序号的临时节点
     *
     * @param path 节点路径前缀
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径（包含序号）
     * @throws Exception Zookeeper异常
     */
    @Override
    public String createEphemeralSequentialNode(String path, String data) throws Exception {
        String result = curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建带序号临时节点成功: {}", result);
        return result;
    }

    /**
     * 获取节点数据
     *
     * @param path 节点路径
     * @return 节点数据（字符串），节点不存在时返回null
     * @throws Exception Zookeeper异常
     */
    @Override
    public String getData(String path) throws Exception {
        byte[] data = curatorFramework.getData().forPath(path);
        if (data == null) {
            return null;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 更新节点数据
     *
     * @param path 节点路径
     * @param data 新的节点数据（字符串）
     * @throws Exception Zookeeper异常
     */
    @Override
    public void setData(String path, String data) throws Exception {
        curatorFramework.setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("更新节点数据成功: {}", path);
    }

    /**
     * 删除节点
     * 如果节点有子节点，会一并删除
     *
     * @param path 节点路径
     * @throws Exception Zookeeper异常
     */
    @Override
    public void deleteNode(String path) throws Exception {
        curatorFramework.delete().deletingChildrenIfNeeded().forPath(path);
        log.info("删除节点成功: {}", path);
    }

    /**
     * 检查节点是否存在
     *
     * @param path 节点路径
     * @return true表示节点存在，false表示节点不存在
     * @throws Exception Zookeeper异常
     */
    @Override
    public boolean exists(String path) throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(path);
        return stat != null;
    }

    /**
     * 获取子节点列表
     *
     * @param path 父节点路径
     * @return 子节点名称列表
     * @throws Exception Zookeeper异常
     */
    @Override
    public List<String> getChildren(String path) throws Exception {
        return curatorFramework.getChildren().forPath(path);
    }

    /**
     * 获取分布式锁（使用Curator的InterProcessMutex）
     *
     * @param lockName 锁名称
     * @return 锁名称
     * @throws Exception 获取锁异常
     */
    @Override
    public String acquireLock(String lockName) throws Exception {
        String lockPath = "/locks/" + lockName;
        // 创建分布式锁实例
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, lockPath);
        // 获取锁（阻塞式）
        lock.acquire();
        // 缓存锁实例，用于后续释放
        lockMap.put(lockName, lock);
        log.info("获取分布式锁成功: {}", lockName);
        return lockName;
    }

    /**
     * 释放分布式锁
     *
     * @param lockName 锁名称
     * @throws Exception 释放锁异常
     */
    @Override
    public void releaseLock(String lockName) throws Exception {
        InterProcessMutex lock = lockMap.get(lockName);
        if (lock != null) {
            lock.release();
            lockMap.remove(lockName);
            log.info("释放分布式锁成功: {}", lockName);
        }
    }

    /**
     * 启动Leader选举
     * 使用Curator的LeaderSelector实现，支持自动重新加入选举
     *
     * @param leaderPath 选举路径（用于区分不同的选举组）
     * @param candidateId 候选者ID（标识当前节点）
     * @return 选举启动信息
     * @throws Exception 选举异常
     */
    @Override
    public String electLeader(String leaderPath, String candidateId) throws Exception {
        String path = "/leaders/" + leaderPath;

        // 创建LeaderSelector实例
        LeaderSelector leaderSelector = new LeaderSelector(curatorFramework, path,
                new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework client) throws Exception {
                        // 当选为Leader时触发
                        log.info("节点 {} 成为Leader", candidateId);
                        isLeader.set(true);
                        try {
                            // 保持Leader身份，直到线程被中断
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException e) {
                            log.info("Leader被中断");
                            Thread.currentThread().interrupt();
                        } finally {
                            // 失去Leader身份时触发
                            isLeader.set(false);
                            log.info("节点 {} 不再是Leader", candidateId);
                        }
                    }
                });

        // 设置自动重新加入选举（当Leader失效时，自动重新参与选举）
        leaderSelector.autoRequeue();
        // 启动选举
        leaderSelector.start();
        // 缓存LeaderSelector实例
        leaderSelectorMap.put(leaderPath, leaderSelector);

        return "Leader选举已启动，候选ID: " + candidateId;
    }

    /**
     * 检查当前节点是否为Leader
     *
     * @param leaderPath 选举路径
     * @param candidateId 候选者ID
     * @return true表示当前节点是Leader，false表示不是
     * @throws Exception 检查异常
     */
    @Override
    public boolean isLeader(String leaderPath, String candidateId) throws Exception {
        return isLeader.get();
    }

    /**
     * 获取分布式计数器的值
     *
     * @param counterPath 计数器名称
     * @return 计数器当前值
     * @throws Exception 获取计数器异常
     */
    @Override
    public long getCounter(String counterPath) throws Exception {
        String path = "/counters/" + counterPath;

        // 使用computeIfAbsent确保只创建一次SharedCount实例
        SharedCount counter = counterMap.computeIfAbsent(counterPath, k -> {
            try {
                SharedCount sharedCount = new SharedCount(curatorFramework, path, 0);
                sharedCount.start();
                return sharedCount;
            } catch (Exception e) {
                throw new RuntimeException("创建计数器失败", e);
            }
        });

        return counter.getCount();
    }

    /**
     * 增加分布式计数器
     *
     * @param counterPath 计数器名称
     * @return 增加后的计数器值
     * @throws Exception 更新计数器异常
     */
    @Override
    public long incrementCounter(String counterPath) throws Exception {
        String path = "/counters/" + counterPath;

        SharedCount counter = counterMap.computeIfAbsent(counterPath, k -> {
            try {
                SharedCount sharedCount = new SharedCount(curatorFramework, path, 0);
                sharedCount.start();
                return sharedCount;
            } catch (Exception e) {
                throw new RuntimeException("创建计数器失败", e);
            }
        });

        int newCount = counter.getCount() + 1;
        counter.setCount(newCount);
        log.info("计数器 {} 增加到: {}", counterPath, newCount);
        return newCount;
    }

    /**
     * 减少分布式计数器
     *
     * @param counterPath 计数器名称
     * @return 减少后的计数器值
     * @throws Exception 更新计数器异常
     */
    @Override
    public long decrementCounter(String counterPath) throws Exception {
        String path = "/counters/" + counterPath;

        SharedCount counter = counterMap.computeIfAbsent(counterPath, k -> {
            try {
                SharedCount sharedCount = new SharedCount(curatorFramework, path, 0);
                sharedCount.start();
                return sharedCount;
            } catch (Exception e) {
                throw new RuntimeException("创建计数器失败", e);
            }
        });

        int newCount = counter.getCount() - 1;
        counter.setCount(newCount);
        log.info("计数器 {} 减少到: {}", counterPath, newCount);
        return newCount;
    }

    /**
     * 重置分布式计数器（设置为0）
     *
     * @param counterPath 计数器名称
     * @throws Exception 更新计数器异常
     */
    @Override
    public void resetCounter(String counterPath) throws Exception {
        String path = "/counters/" + counterPath;

        SharedCount counter = counterMap.computeIfAbsent(counterPath, k -> {
            try {
                SharedCount sharedCount = new SharedCount(curatorFramework, path, 0);
                sharedCount.start();
                return sharedCount;
            } catch (Exception e) {
                throw new RuntimeException("创建计数器失败", e);
            }
        });

        counter.setCount(0);
        log.info("计数器 {} 已重置", counterPath);
    }

    /**
     * 注册CuratorCache监听器
     * 监听指定路径下的节点创建、变更、删除事件
     *
     * @param path 要监听的节点路径
     * @throws Exception 注册监听器异常
     */
    @Override
    public void registerCacheListener(String path) throws Exception {
        // 创建CuratorCache实例，用于缓存和监听节点变化
        CuratorCache cache = CuratorCache.build(curatorFramework, path);

        // 构建监听器，处理三种事件：创建、变更、删除
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node -> {
                    // 节点创建事件
                    log.info("节点创建: {} = {}", node.getPath(),
                            new String(node.getData(), StandardCharsets.UTF_8));
                })
                .forChanges((oldNode, node) -> {
                    // 节点变更事件
                    log.info("节点变更: {} 从 {} 变为 {}", node.getPath(),
                            oldNode != null ? new String(oldNode.getData(), StandardCharsets.UTF_8) : "null",
                            new String(node.getData(), StandardCharsets.UTF_8));
                })
                .forDeletes(node -> {
                    // 节点删除事件
                    log.info("节点删除: {}", node.getPath());
                })
                .build();

        // 添加监听器并启动Cache
        cache.listenable().addListener(listener);
        cache.start();
        // 缓存CuratorCache实例
        cacheMap.put(path, cache);

        log.info("CuratorCache监听器已注册: {}", path);
    }
}
