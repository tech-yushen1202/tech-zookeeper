package org.tech.zookeeper.nativeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Zookeeper原生客户端演示应用启动类
 * 演示内容：
 * - 创建节点
 * - 删除节点
 * - 更新节点数据
 * - 查询节点数据
 * - 监听节点变化
 * - 分布式锁
 */
@SpringBootApplication
public class NativeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NativeApplication.class, args);
    }
}
