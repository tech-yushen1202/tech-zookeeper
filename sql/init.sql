CREATE DATABASE IF NOT EXISTS tech_zookeeper_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tech_zookeeper_demo;

CREATE TABLE IF NOT EXISTS zookeeper_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_path VARCHAR(512) NOT NULL UNIQUE COMMENT 'Zookeeper节点路径',
    node_data TEXT COMMENT '节点数据',
    node_type VARCHAR(32) NOT NULL COMMENT '节点类型: PERSISTENT, EPHEMERAL, PERSISTENT_SEQUENTIAL, EPHEMERAL_SEQUENTIAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_node_path (node_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Zookeeper节点记录';

CREATE TABLE IF NOT EXISTS distributed_lock_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lock_name VARCHAR(128) NOT NULL COMMENT '锁名称',
    lock_holder VARCHAR(256) COMMENT '锁持有者标识',
    acquired_at DATETIME COMMENT '获取锁时间',
    released_at DATETIME COMMENT '释放锁时间',
    status VARCHAR(32) NOT NULL DEFAULT 'LOCKED' COMMENT '状态: LOCKED, RELEASED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lock_name (lock_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分布式锁记录';

CREATE TABLE IF NOT EXISTS leader_election_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    leader_path VARCHAR(256) NOT NULL COMMENT 'Leader选举路径',
    candidate_id VARCHAR(128) NOT NULL COMMENT '候选者ID',
    is_leader TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为Leader',
    elected_at DATETIME COMMENT '当选时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_leader_path_candidate (leader_path, candidate_id),
    INDEX idx_is_leader (is_leader)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Leader选举记录';

CREATE TABLE IF NOT EXISTS counter_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counter_name VARCHAR(128) NOT NULL UNIQUE COMMENT '计数器名称',
    counter_value BIGINT NOT NULL DEFAULT 0 COMMENT '计数器值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_counter_name (counter_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分布式计数器记录';

CREATE TABLE IF NOT EXISTS watcher_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_path VARCHAR(512) NOT NULL COMMENT '节点路径',
    event_type VARCHAR(32) NOT NULL COMMENT '事件类型: NODE_CREATED, NODE_DELETED, NODE_DATA_CHANGED, NODE_CHILDREN_CHANGED',
    event_data TEXT COMMENT '事件数据',
    occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_node_path (node_path),
    INDEX idx_event_type (event_type),
    INDEX idx_occurred_at (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监听器事件记录';
