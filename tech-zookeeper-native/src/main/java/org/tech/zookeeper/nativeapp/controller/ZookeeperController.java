package org.tech.zookeeper.nativeapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tech.zookeeper.nativeapp.service.ZookeeperService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zookeeper原生客户端演示控制器
 * 提供REST API演示Zookeeper基本操作：
 * 1. 创建节点（持久、临时、带序号）
 * 2. 查询节点数据
 * 3. 更新节点数据
 * 4. 删除节点
 * 5. 检查节点存在
 * 6. 获取子节点列表
 * 7. 分布式锁
 * 8. 监听器演示
 */
@Slf4j
@RestController
@RequestMapping("/api/zookeeper/native")
public class ZookeeperController {

    @Autowired
    private ZookeeperService zookeeperService;

    /**
     * 创建持久节点
     * POST /api/zookeeper/native/node/persistent
     * { "path": "/demo/node1", "data": "hello" }
     */
    @PostMapping("/node/persistent")
    public Map<String, Object> createPersistentNode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String path = request.get("path");
            String data = request.get("data");
            String result = zookeeperService.createPersistentNode(path, data);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("path", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("创建持久节点失败", e);
        }
        return response;
    }

    /**
     * 创建临时节点
     * POST /api/zookeeper/native/node/ephemeral
     * { "path": "/demo/temp1", "data": "temp-data" }
     */
    @PostMapping("/node/ephemeral")
    public Map<String, Object> createEphemeralNode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String path = request.get("path");
            String data = request.get("data");
            String result = zookeeperService.createEphemeralNode(path, data);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("path", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("创建临时节点失败", e);
        }
        return response;
    }

    /**
     * 创建带序号持久节点
     * POST /api/zookeeper/native/node/persistent-seq
     * { "path": "/demo/seq-", "data": "seq-data" }
     */
    @PostMapping("/node/persistent-seq")
    public Map<String, Object> createPersistentSequentialNode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String path = request.get("path");
            String data = request.get("data");
            String result = zookeeperService.createPersistentSequentialNode(path, data);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("path", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("创建带序号持久节点失败", e);
        }
        return response;
    }

    /**
     * 创建带序号临时节点
     * POST /api/zookeeper/native/node/ephemeral-seq
     * { "path": "/demo/lock-", "data": "lock-data" }
     */
    @PostMapping("/node/ephemeral-seq")
    public Map<String, Object> createEphemeralSequentialNode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String path = request.get("path");
            String data = request.get("data");
            String result = zookeeperService.createEphemeralSequentialNode(path, data);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("path", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("创建带序号临时节点失败", e);
        }
        return response;
    }

    /**
     * 获取节点数据
     * GET /api/zookeeper/native/node/{path}
     */
    @GetMapping("/node/{path:.+}")
    public Map<String, Object> getData(@PathVariable String path) {
        Map<String, Object> response = new HashMap<>();
        try {
            String data = zookeeperService.getData(path);
            response.put("success", true);
            response.put("data", data);
            response.put("path", path);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("获取节点数据失败", e);
        }
        return response;
    }

    /**
     * 更新节点数据
     * PUT /api/zookeeper/native/node/{path}
     * { "data": "new-data" }
     */
    @PutMapping("/node/{path:.+}")
    public Map<String, Object> setData(@PathVariable String path, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String data = request.get("data");
            String result = zookeeperService.setData(path, data);
            response.put("success", true);
            response.put("message", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("更新节点数据失败", e);
        }
        return response;
    }

    /**
     * 删除节点
     * DELETE /api/zookeeper/native/node/{path}
     */
    @DeleteMapping("/node/{path:.+}")
    public Map<String, Object> deleteNode(@PathVariable String path) {
        Map<String, Object> response = new HashMap<>();
        try {
            zookeeperService.deleteNode(path);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("删除节点失败", e);
        }
        return response;
    }

    /**
     * 检查节点是否存在
     * GET /api/zookeeper/native/node/exists/{path}
     */
    @GetMapping("/node/exists/{path:.+}")
    public Map<String, Object> exists(@PathVariable String path) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = zookeeperService.exists(path);
            response.put("success", true);
            response.put("exists", exists);
            response.put("path", path);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("检查节点存在失败", e);
        }
        return response;
    }

    /**
     * 获取子节点列表
     * GET /api/zookeeper/native/node/children/{path}
     */
    @GetMapping("/node/children/{path:.+}")
    public Map<String, Object> getChildren(@PathVariable String path) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> children = zookeeperService.getChildren(path);
            response.put("success", true);
            response.put("children", children);
            response.put("path", path);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("获取子节点列表失败", e);
        }
        return response;
    }

    /**
     * 获取根节点子节点列表
     * GET /api/zookeeper/native/node/children
     */
    @GetMapping("/node/children")
    public Map<String, Object> getRootChildren() {
        return getChildren("/");
    }

    /**
     * 获取分布式锁
     * POST /api/zookeeper/native/lock/acquire/{lockName}
     */
    @PostMapping("/lock/acquire/{lockName}")
    public Map<String, Object> acquireLock(@PathVariable String lockName) {
        Map<String, Object> response = new HashMap<>();
        try {
            String lockId = zookeeperService.acquireLock(lockName);
            response.put("success", true);
            response.put("message", "获取锁成功");
            response.put("lockId", lockId);
            response.put("lockName", lockName);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("获取分布式锁失败", e);
        }
        return response;
    }

    /**
     * 释放分布式锁
     * POST /api/zookeeper/native/lock/release
     */
    @PostMapping("/lock/release")
    public Map<String, Object> releaseLock() {
        Map<String, Object> response = new HashMap<>();
        try {
            zookeeperService.releaseLock();
            response.put("success", true);
            response.put("message", "释放锁成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("释放分布式锁失败", e);
        }
        return response;
    }

    /**
     * 注册监听器
     * POST /api/zookeeper/native/watcher/{path}
     */
    @PostMapping("/watcher/{path:.+}")
    public Map<String, Object> testWatcher(@PathVariable String path) {
        Map<String, Object> response = new HashMap<>();
        try {
            zookeeperService.testWatcher(path);
            response.put("success", true);
            response.put("message", "监听器注册成功");
            response.put("path", path);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("注册监听器失败", e);
        }
        return response;
    }

    /**
     * 健康检查
     * GET /api/zookeeper/native/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("service", "tech-zookeeper-native");
        response.put("status", "UP");
        return response;
    }
}
