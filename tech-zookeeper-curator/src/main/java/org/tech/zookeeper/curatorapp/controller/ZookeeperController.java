package org.tech.zookeeper.curatorapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tech.zookeeper.curatorapp.service.ZookeeperService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Zookeeper Curator客户端演示控制器
 * 提供REST API演示Zookeeper基本操作：
 * 1. 创建节点（持久、临时、带序号）
 * 2. 查询节点数据
 * 3. 更新节点数据
 * 4. 删除节点
 * 5. 检查节点存在
 * 6. 获取子节点列表
 * 7. 分布式锁(InterProcessMutex)
 * 8. Leader选举
 * 9. 分布式计数器
 * 10. CuratorCache监听器
 */
@Slf4j
@RestController
@RequestMapping("/api/zookeeper/curator")
public class ZookeeperController {

    @Autowired
    private ZookeeperService zookeeperService;

    /**
     * 创建持久节点
     * POST /api/zookeeper/curator/node/persistent
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
     * POST /api/zookeeper/curator/node/ephemeral
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
     * POST /api/zookeeper/curator/node/persistent-seq
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
     * POST /api/zookeeper/curator/node/ephemeral-seq
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
     * GET /api/zookeeper/curator/node/{path}
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
     * PUT /api/zookeeper/curator/node/{path}
     * { "data": "new-data" }
     */
    @PutMapping("/node/{path:.+}")
    public Map<String, Object> setData(@PathVariable String path, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String data = request.get("data");
            zookeeperService.setData(path, data);
            response.put("success", true);
            response.put("message", "更新成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("更新节点数据失败", e);
        }
        return response;
    }

    /**
     * 删除节点
     * DELETE /api/zookeeper/curator/node/{path}
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
     * GET /api/zookeeper/curator/node/exists/{path}
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
     * GET /api/zookeeper/curator/node/children/{path}
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
     * GET /api/zookeeper/curator/node/children
     */
    @GetMapping("/node/children")
    public Map<String, Object> getRootChildren() {
        return getChildren("/");
    }

    /**
     * 获取分布式锁
     * POST /api/zookeeper/curator/lock/acquire/{lockName}
     */
    @PostMapping("/lock/acquire/{lockName}")
    public Map<String, Object> acquireLock(@PathVariable String lockName) {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = zookeeperService.acquireLock(lockName);
            response.put("success", true);
            response.put("message", "获取锁成功");
            response.put("lockName", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("获取分布式锁失败", e);
        }
        return response;
    }

    /**
     * 释放分布式锁
     * POST /api/zookeeper/curator/lock/release/{lockName}
     */
    @PostMapping("/lock/release/{lockName}")
    public Map<String, Object> releaseLock(@PathVariable String lockName) {
        Map<String, Object> response = new HashMap<>();
        try {
            zookeeperService.releaseLock(lockName);
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
     * 启动Leader选举
     * POST /api/zookeeper/curator/leader/elect/{leaderPath}
     */
    @PostMapping("/leader/elect/{leaderPath}")
    public Map<String, Object> electLeader(@PathVariable String leaderPath) {
        Map<String, Object> response = new HashMap<>();
        try {
            String candidateId = UUID.randomUUID().toString().substring(0, 8);
            String result = zookeeperService.electLeader(leaderPath, candidateId);
            response.put("success", true);
            response.put("message", result);
            response.put("candidateId", candidateId);
            response.put("leaderPath", leaderPath);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("Leader选举失败", e);
        }
        return response;
    }

    /**
     * 检查是否为Leader
     * GET /api/zookeeper/curator/leader/is-leader/{leaderPath}
     */
    @GetMapping("/leader/is-leader/{leaderPath}")
    public Map<String, Object> isLeader(@PathVariable String leaderPath) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean leader = zookeeperService.isLeader(leaderPath, "");
            response.put("success", true);
            response.put("isLeader", leader);
            response.put("leaderPath", leaderPath);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("检查Leader失败", e);
        }
        return response;
    }

    /**
     * 获取计数器值
     * GET /api/zookeeper/curator/counter/{counterPath}
     */
    @GetMapping("/counter/{counterPath}")
    public Map<String, Object> getCounter(@PathVariable String counterPath) {
        Map<String, Object> response = new HashMap<>();
        try {
            long value = zookeeperService.getCounter(counterPath);
            response.put("success", true);
            response.put("value", value);
            response.put("counterPath", counterPath);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("获取计数器失败", e);
        }
        return response;
    }

    /**
     * 增加计数器
     * POST /api/zookeeper/curator/counter/increment/{counterPath}
     */
    @PostMapping("/counter/increment/{counterPath}")
    public Map<String, Object> incrementCounter(@PathVariable String counterPath) {
        Map<String, Object> response = new HashMap<>();
        try {
            long value = zookeeperService.incrementCounter(counterPath);
            response.put("success", true);
            response.put("value", value);
            response.put("counterPath", counterPath);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("增加计数器失败", e);
        }
        return response;
    }

    /**
     * 减少计数器
     * POST /api/zookeeper/curator/counter/decrement/{counterPath}
     */
    @PostMapping("/counter/decrement/{counterPath}")
    public Map<String, Object> decrementCounter(@PathVariable String counterPath) {
        Map<String, Object> response = new HashMap<>();
        try {
            long value = zookeeperService.decrementCounter(counterPath);
            response.put("success", true);
            response.put("value", value);
            response.put("counterPath", counterPath);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("减少计数器失败", e);
        }
        return response;
    }

    /**
     * 重置计数器
     * POST /api/zookeeper/curator/counter/reset/{counterPath}
     */
    @PostMapping("/counter/reset/{counterPath}")
    public Map<String, Object> resetCounter(@PathVariable String counterPath) {
        Map<String, Object> response = new HashMap<>();
        try {
            zookeeperService.resetCounter(counterPath);
            response.put("success", true);
            response.put("message", "计数器已重置");
            response.put("counterPath", counterPath);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            log.error("重置计数器失败", e);
        }
        return response;
    }

    /**
     * 注册CuratorCache监听器
     * POST /api/zookeeper/curator/cache/{path}
     */
    @PostMapping("/cache/{path:.+}")
    public Map<String, Object> registerCacheListener(@PathVariable String path) {
        Map<String, Object> response = new HashMap<>();
        try {
            zookeeperService.registerCacheListener(path);
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
     * GET /api/zookeeper/curator/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("service", "tech-zookeeper-curator");
        response.put("status", "UP");
        return response;
    }
}
