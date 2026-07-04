package org.tech.zookeeper.nativeapp.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tech.zookeeper.nativeapp.client.DistributedLock;
import org.tech.zookeeper.nativeapp.client.ZookeeperClient;
import org.tech.zookeeper.nativeapp.service.ZookeeperService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Zookeeper服务实现类
 * 封装Zookeeper原生客户端的操作，提供业务层调用接口
 */
@Slf4j
@Service
public class ZookeeperServiceImpl implements ZookeeperService {

    @Autowired
    private ZookeeperClient zookeeperClient;

    @Autowired
    private DistributedLock distributedLock;

    /**
     * 创建持久节点
     *
     * @param path 节点路径
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public String createPersistentNode(String path, String data) throws KeeperException, InterruptedException {
        String result = zookeeperClient.createPersistentNode(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建持久节点成功: {}", result);
        return result;
    }

    /**
     * 创建临时节点
     *
     * @param path 节点路径
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public String createEphemeralNode(String path, String data) throws KeeperException, InterruptedException {
        String result = zookeeperClient.createEphemeralNode(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建临时节点成功: {}", result);
        return result;
    }

    /**
     * 创建带序号的持久节点
     *
     * @param path 节点路径前缀
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径（包含序号）
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public String createPersistentSequentialNode(String path, String data) throws KeeperException, InterruptedException {
        String result = zookeeperClient.createPersistentSequentialNode(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建带序号持久节点成功: {}", result);
        return result;
    }

    /**
     * 创建带序号的临时节点
     *
     * @param path 节点路径前缀
     * @param data 节点数据（字符串）
     * @return 创建成功的节点路径（包含序号）
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public String createEphemeralSequentialNode(String path, String data) throws KeeperException, InterruptedException {
        String result = zookeeperClient.createEphemeralSequentialNode(path, data.getBytes(StandardCharsets.UTF_8));
        log.info("创建带序号临时节点成功: {}", result);
        return result;
    }

    /**
     * 获取节点数据
     *
     * @param path 节点路径
     * @return 节点数据（字符串），节点不存在时返回null
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public String getData(String path) throws KeeperException, InterruptedException {
        byte[] data = zookeeperClient.getData(path);
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
     * @return "OK"表示更新成功
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public String setData(String path, String data) throws KeeperException, InterruptedException {
        zookeeperClient.setData(path, data.getBytes(StandardCharsets.UTF_8), -1);
        log.info("更新节点数据成功: {}", path);
        return "OK";
    }

    /**
     * 删除节点
     *
     * @param path 节点路径
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public void deleteNode(String path) throws KeeperException, InterruptedException {
        zookeeperClient.deleteNode(path, -1);
        log.info("删除节点成功: {}", path);
    }

    /**
     * 检查节点是否存在
     *
     * @param path 节点路径
     * @return true表示节点存在，false表示节点不存在
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public boolean exists(String path) throws KeeperException, InterruptedException {
        return zookeeperClient.exists(path) != null;
    }

    /**
     * 获取子节点列表
     *
     * @param path 父节点路径
     * @return 子节点名称列表
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        return zookeeperClient.getChildren(path);
    }

    /**
     * 获取分布式锁
     *
     * @param lockName 锁名称
     * @return 锁标识ID（UUID）
     */
    @Override
    public String acquireLock(String lockName) {
        distributedLock.setLockPath(lockName);
        distributedLock.lock();
        String lockId = UUID.randomUUID().toString();
        log.info("获取分布式锁成功, lockName:{}, lockId:{}", lockName, lockId);
        return lockId;
    }

    /**
     * 释放分布式锁
     */
    @Override
    public void releaseLock() {
        distributedLock.unlock();
        log.info("释放分布式锁成功");
    }

    /**
     * 注册节点监听器
     * 监听节点的创建、删除、数据变更事件
     *
     * @param path 要监听的节点路径
     * @throws KeeperException      Zookeeper异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public void testWatcher(String path) throws KeeperException, InterruptedException {
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                log.info("监听器触发: 事件类型={}, 路径={}, 状态={}",
                        event.getType(), event.getPath(), event.getState());
                if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    // 节点数据变更事件
                    try {
                        byte[] data = zookeeperClient.getData(event.getPath());
                        log.info("节点数据变更为: {}", new String(data, StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        log.error("获取变更后数据失败", e);
                    }
                } else if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    // 节点删除事件
                    log.info("节点被删除: {}", event.getPath());
                } else if (event.getType() == Watcher.Event.EventType.NodeCreated) {
                    // 节点创建事件
                    log.info("节点被创建: {}", event.getPath());
                }
            }
        };

        // 为节点注册监听器
        zookeeperClient.existsWithWatcher(path, watcher);
        log.info("已为节点 {} 注册监听器", path);
    }
}
