package com.itskool.util;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class ServiceUtil {
    private final String port;

    private String serviceAddress = null;


    public ServiceUtil(@Value("${server.port}") String port) {
        this.port = port;
    }

    public Faker faker() {
        return Faker.instance();
    }

    public String getServiceAddress() {
        if (serviceAddress == null) {
            serviceAddress = findMyHostname() + "/" + findMyIpAddress() + ":" + port;
        }
        return serviceAddress;
    }

    private String findMyHostname() {
        try {
            return InetAddress.getLocalHost()
                    .getHostName();
        }
        catch (UnknownHostException e) {
            return "unknown host name";
        }
    }

    private String findMyIpAddress() {
        try {
            return InetAddress.getLocalHost()
                    .getHostAddress();
        }
        catch (UnknownHostException e) {
            return "unknown IP address";
        }
    }
}
