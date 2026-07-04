package org.tech.zookeeper.curatorapp.service;

import java.util.List;

public interface ZookeeperService {

    String createPersistentNode(String path, String data) throws Exception;

    String createEphemeralNode(String path, String data) throws Exception;

    String createPersistentSequentialNode(String path, String data) throws Exception;

    String createEphemeralSequentialNode(String path, String data) throws Exception;

    String getData(String path) throws Exception;

    void setData(String path, String data) throws Exception;

    void deleteNode(String path) throws Exception;

    boolean exists(String path) throws Exception;

    List<String> getChildren(String path) throws Exception;

    String acquireLock(String lockName) throws Exception;

    void releaseLock(String lockName) throws Exception;

    String electLeader(String leaderPath, String candidateId) throws Exception;

    boolean isLeader(String leaderPath, String candidateId) throws Exception;

    long getCounter(String counterPath) throws Exception;

    long incrementCounter(String counterPath) throws Exception;

    long decrementCounter(String counterPath) throws Exception;

    void resetCounter(String counterPath) throws Exception;

    void registerCacheListener(String path) throws Exception;
}
