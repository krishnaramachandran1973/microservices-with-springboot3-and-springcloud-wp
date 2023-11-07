package com.itskool.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class ReviewProperties {
    private Integer threadPoolSize;
    private Integer taskQueueSize;
    private String eurekaServer;
    private String eurekaUsername;
    private String eurekaPassword;
}
