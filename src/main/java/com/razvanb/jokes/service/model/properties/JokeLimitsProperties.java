package com.razvanb.jokes.service.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("app")
public record JokeLimitsProperties(int batchSize, int jokesLimit, int defaultJokesCount) {
}