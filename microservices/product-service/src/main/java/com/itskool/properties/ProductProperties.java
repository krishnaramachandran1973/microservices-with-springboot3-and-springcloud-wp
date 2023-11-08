package com.itskool.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class ProductProperties {
    private String eurekaServer;
    private String eurekaUsername;
    private String eurekaPassword;
    private String configServer;
}
