package org.tech.zookeeper.curatorapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperConfig {

    private String host = "localhost";
    private int port = 2181;
    private int sessionTimeout = 30000;
    private int connectionTimeout = 10000;

    public String getConnectString() {
        return host + ":" + port;
    }
}
