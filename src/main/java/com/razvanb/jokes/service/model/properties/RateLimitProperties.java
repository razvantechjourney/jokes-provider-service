package com.razvanb.jokes.service.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.resilience.rate")
public record RateLimitProperties(int requestsLimit, int limitForSeconds, long resetAfterMillis) {
}
