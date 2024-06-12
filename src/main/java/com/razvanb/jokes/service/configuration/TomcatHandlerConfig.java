package com.razvanb.jokes.service.configuration;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class TomcatHandlerConfig {
    // Handle each http request on a new virtual thread
    @Bean
    public TomcatProtocolHandlerCustomizer<?> virtualThreadPerTaskHandler() {
        return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
