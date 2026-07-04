USE tech_zookeeper_demo;

INSERT INTO zookeeper_nodes (node_path, node_data, node_type) VALUES
('/demo/test-node', 'test-data', 'PERSISTENT'),
('/demo/config', '{"app": "tech-zookeeper", "version": "1.0.0"}', 'PERSISTENT');

INSERT INTO counter_records (counter_name, counter_value) VALUES
('page_views', 0),
('api_calls', 0),
('service_invocations', 0);
