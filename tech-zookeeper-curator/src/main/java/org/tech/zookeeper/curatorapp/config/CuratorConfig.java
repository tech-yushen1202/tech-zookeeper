package org.tech.zookeeper.curatorapp.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Curator客户端配置类
 * 配置并创建CuratorFramework实例，提供连接管理、重试策略等功能
 */
@Slf4j
@Configuration
public class CuratorConfig {

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    /**
     * 创建CuratorFramework实例
     * 使用指数退避重试策略，自动处理连接失败和重连
     *
     * @return CuratorFramework客户端实例
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        // 创建指数退避重试策略
        // 参数说明：
        // initialSleepMs: 初始重试等待时间（毫秒）
        // maxRetries: 最大重试次数
        // maxSleepMs: 最大重试等待时间（毫秒）
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                1000,
                3,
                10000
        );

        // 构建CuratorFramework客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                // Zookeeper连接地址
                .connectString(zookeeperConfig.getConnectString())
                // 会话超时时间（毫秒）
                .sessionTimeoutMs(zookeeperConfig.getSessionTimeout())
                // 连接超时时间（毫秒）
                .connectionTimeoutMs(zookeeperConfig.getConnectionTimeout())
                // 重试策略
                .retryPolicy(retryPolicy)
                // 命名空间，所有操作都会在该命名空间下进行
                .namespace("tech-curator")
                .build();

        log.info("Curator客户端初始化完成，连接地址: {}", zookeeperConfig.getConnectString());
        return client;
    }
}
