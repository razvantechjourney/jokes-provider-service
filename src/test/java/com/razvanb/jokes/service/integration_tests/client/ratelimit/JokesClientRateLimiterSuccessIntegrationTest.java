package com.razvanb.jokes.service.integration_tests.client.ratelimit;

import com.razvanb.jokes.service.integration_tests.client.JokesClientBaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
        "app.resilience.rate.requestsLimit=2",
        "app.resilience.rate.limitForSeconds=1"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JokesClientRateLimiterSuccessIntegrationTest extends JokesClientBaseIntegrationTest {

    @Test
    public void givenRateLimitedAtTwoRequests_whenGetRandomJokeWithinLimit_thenAllowRequests() {
        // Make the allowed calls within the limit
        jokesClient.getRandomJoke();
        jokesClient.getRandomJoke();

        assertCustomRateLimiterMetricCountEquals("custom.rateLimiter.success.count", 2);
        assertMetricDoesNotExist("custom.rateLimiter.failure.count");
    }
}
