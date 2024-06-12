package com.razvanb.jokes.service.configuration;

import com.razvanb.jokes.service.exception.JokesClientRequestRateExceededException;
import com.razvanb.jokes.service.model.properties.RateLimitProperties;
import com.razvanb.jokes.service.model.properties.RetryProperties;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class ResilienceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilienceConfig.class);
    private final RetryProperties retryProperties;
    private final RateLimitProperties rateLimitProperties;

    public ResilienceConfig(RetryProperties retryProperties, RateLimitProperties rateLimitProperties) {
        this.retryProperties = retryProperties;
        this.rateLimitProperties = rateLimitProperties;
    }

    @Bean
    public RateLimiter jokeApiRateLimiter(
            @Value("${app.resilience.rate.instanceName}") String rateLimiterInstanceName,
            RateLimiterRegistry rateLimiterRegistry,
            MeterRegistry meterRegistry) {

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(rateLimitProperties.requestsLimit())
                .limitRefreshPeriod(Duration.ofSeconds(rateLimitProperties.limitForSeconds()))
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterInstanceName, config);
        rateLimiter.getEventPublisher()
                .onSuccess(event -> {
                    LOGGER.debug("Rate limiter success: {}", event);
                    meterRegistry.counter("custom.rateLimiter.success.count", "name", rateLimiterInstanceName).increment();
                })
                .onFailure(event -> {
                    LOGGER.debug("Rate limiter failure: {}", event);
                    meterRegistry.counter("custom.rateLimiter.failure.count", "name", rateLimiterInstanceName).increment();
                });

        return rateLimiter;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryProperties.retryAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        retryProperties.initialDelayMillis(),
                        retryProperties.delayMultiplier(),
                        retryProperties.maxDelayMillis()))
                .retryExceptions(HttpServerErrorException.class, ResourceAccessException.class, IOException.class)
                .ignoreExceptions(HttpClientErrorException.class, JokesClientRequestRateExceededException.class)
                .build();

        return RetryRegistry.of(config);
    }

    @Bean
    public Retry jokeApiRetry(@Value("${app.resilience.retry.instanceName}") String retryInstanceName,
                              RetryRegistry retryRegistry, MeterRegistry meterRegistry) {
        Retry retry = retryRegistry.retry(retryInstanceName);
        // Register a custom metric for retries
        retry.getEventPublisher().onRetry(event -> {
            LOGGER.debug("Retry event: {}", event);
            meterRegistry.counter("custom.retry.count", "name", retryInstanceName).increment();
        });

        return retry;
    }

    @Bean
    public TaggedRetryMetrics taggedRetryMetrics(RetryRegistry retryRegistry, MeterRegistry meterRegistry) {
        TaggedRetryMetrics taggedRetryMetrics = TaggedRetryMetrics.ofRetryRegistry(retryRegistry);
        taggedRetryMetrics.bindTo(meterRegistry);
        return taggedRetryMetrics;
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
