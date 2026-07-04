package org.tech.zookeeper.nativeapp.service;

import org.apache.zookeeper.KeeperException;

import java.util.List;

public interface ZookeeperService {

    String createPersistentNode(String path, String data) throws KeeperException, InterruptedException;

    String createEphemeralNode(String path, String data) throws KeeperException, InterruptedException;

    String createPersistentSequentialNode(String path, String data) throws KeeperException, InterruptedException;

    String createEphemeralSequentialNode(String path, String data) throws KeeperException, InterruptedException;

    String getData(String path) throws KeeperException, InterruptedException;

    String setData(String path, String data) throws KeeperException, InterruptedException;

    void deleteNode(String path) throws KeeperException, InterruptedException;

    boolean exists(String path) throws KeeperException, InterruptedException;

    List<String> getChildren(String path) throws KeeperException, InterruptedException;

    String acquireLock(String lockName);

    void releaseLock();

    void testWatcher(String path) throws KeeperException, InterruptedException;
}
