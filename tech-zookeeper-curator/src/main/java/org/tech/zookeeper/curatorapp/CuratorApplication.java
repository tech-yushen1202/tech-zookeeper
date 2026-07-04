package org.tech.zookeeper.curatorapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Zookeeper Curator客户端演示应用启动类
 * 演示内容：
 * - 创建节点
 * - 删除节点
 * - 更新节点数据
 * - 查询节点数据
 * - 监听节点变化(Curator Cache)
 * - 分布式锁(InterProcessMutex)
 * - Leader选举
 * - 分布式计数器
 */
@SpringBootApplication
public class CuratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuratorApplication.class, args);
    }
}
