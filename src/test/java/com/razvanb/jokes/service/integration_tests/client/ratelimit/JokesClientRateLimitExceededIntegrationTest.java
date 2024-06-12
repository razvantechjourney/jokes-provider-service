package com.razvanb.jokes.service.integration_tests.client.ratelimit;

import com.razvanb.jokes.service.integration_tests.client.JokesClientBaseIntegrationTest;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "app.resilience.rate.requestsLimit=1",
        "app.resilience.rate.limitForSeconds=1"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JokesClientRateLimitExceededIntegrationTest extends JokesClientBaseIntegrationTest {
    @Test
    public void givenConfiguredRateLimiter_whenGetRandomJokeExceedRateLimit_thenThrowRequestNotPermitted() {
        // Make the allowed call within the limit
        jokesClient.getRandomJoke();

        // Exceed the rate limit
        assertThrows(RequestNotPermitted.class, () -> jokesClient.getRandomJoke());

        assertCustomRateLimiterMetricCountEquals("custom.rateLimiter.success.count", 1);
        assertCustomRateLimiterMetricCountEquals("custom.rateLimiter.failure.count", 1);
    }
}
