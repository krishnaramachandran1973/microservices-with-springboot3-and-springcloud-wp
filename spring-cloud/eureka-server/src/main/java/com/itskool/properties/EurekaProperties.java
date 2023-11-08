package com.itskool.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class EurekaProperties {
    private String eurekaUsername;
    private String eurekaPassword;
    private String configServer;
}
