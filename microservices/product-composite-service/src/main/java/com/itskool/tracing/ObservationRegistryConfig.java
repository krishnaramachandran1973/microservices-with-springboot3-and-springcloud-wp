package com.itskool.tracing;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class ObservationRegistryConfig implements ObservationRegistryCustomizer<ObservationRegistry> {

    private final BuildProperties buildProperties;

    @Override
    public void customize(final ObservationRegistry registry) {
        registry.observationConfig()
                .observationFilter(new BuildInfoObservationFilter(buildProperties));
    }
}