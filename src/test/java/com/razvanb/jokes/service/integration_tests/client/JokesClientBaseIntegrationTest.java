package com.razvanb.jokes.service.integration_tests.client;


import com.razvanb.jokes.service.client.JokesClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
public class JokesClientBaseIntegrationTest {
    @Autowired
    public JokesClient jokesClient;

    @Autowired
    public MeterRegistry meterRegistry;

    @MockBean
    public RestTemplate restTemplate;

    @Value("${app.joke-api.url}")
    public String jokeApiUrl;

    @Value("${app.resilience.retry.instanceName}")
    public String retryInstanceName;

    public void assertCustomRateLimiterMetricCountEquals(String metricName, double expectedCount) {
        assertEquals(expectedCount, meterRegistry.get(metricName).counter().count(), 0.1);
    }

    public void assertCustomRetryMetricCountEquals(int retryCount) {
        assertEquals(retryCount, meterRegistry.find("custom.retry.count")
                .tags("name", "jokeApiRetry")
                .counter()
                .count());
    }

    protected void assertMetricDoesNotExist(String metricName) {
        assertNull(meterRegistry.find(metricName).counter(), "Meter with name '" + metricName + "' should not exist");
    }

    public void assertRetriedAndSucceededMetricTriggered() {
        checkMetricsState(1, 0, 0, 0);
    }

    public void assertRetriedAndFailedMetricTriggered() {
        checkMetricsState(0, 1, 0, 0);
    }

    public void assertFailedWithoutRetryMetricTriggered() {
        checkMetricsState(0, 0, 1, 0);
    }

    public void assertFailedWithRetryMetricTriggered() {
        checkMetricsState(0, 0, 0, 1);
    }

    private void checkMetricsState(int retriedAndSucceeded, int retriedAndFailed, int failedWithoutRetry, int failedWithRetry) {
        assertThat(meterRegistry.find("resilience4j.retry.calls")
                .tags("name", retryInstanceName, "kind", "successful_with_retry")
                .functionCounter().count()).isEqualTo(retriedAndSucceeded);

        assertThat(meterRegistry.find("resilience4j.retry.calls")
                .tags("name", retryInstanceName, "kind", "failed_with_retry")
                .functionCounter().count()).isEqualTo(retriedAndFailed);

        assertThat(meterRegistry.find("resilience4j.retry.calls")
                .tags("name", retryInstanceName, "kind", "successful_without_retry")
                .functionCounter().count()).isEqualTo(failedWithoutRetry);

        assertThat(meterRegistry.find("resilience4j.retry.calls")
                .tags("name", retryInstanceName, "kind", "failed_without_retry")
                .functionCounter().count()).isEqualTo(failedWithRetry);
    }
}
