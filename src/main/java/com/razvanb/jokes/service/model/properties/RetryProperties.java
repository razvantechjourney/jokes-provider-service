package com.razvanb.jokes.service.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.resilience.retry")
public record RetryProperties(int retryAttempts, long initialDelayMillis, long maxDelayMillis, int delayMultiplier) {
}
