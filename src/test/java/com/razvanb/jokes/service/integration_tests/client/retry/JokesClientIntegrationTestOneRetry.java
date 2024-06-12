package com.razvanb.jokes.service.integration_tests.client.retry;

import com.razvanb.jokes.service.integration_tests.client.JokesClientBaseIntegrationTest;
import com.razvanb.jokes.service.model.JokeData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"app.resilience.retry.retry-attempts=1"})
public class JokesClientIntegrationTestOneRetry extends JokesClientBaseIntegrationTest {


    @Test
    public void givenRetryAttemptsExceeded_whenGetJokeFromExternalApi_thenRetriesOnceAndThrowsException() {
        when(restTemplate.getForObject(jokeApiUrl, JokeData.class))
                .thenThrow(new ResourceAccessException("I/O Exception"))
                .thenThrow(new ResourceAccessException("I/O Exception"))
                .thenReturn(new JokeData(1, "test", "setup", "punchline"));

        assertThrows(ResourceAccessException.class, () -> jokesClient.getRandomJoke());

        verify(restTemplate, times(1)).getForObject(jokeApiUrl, JokeData.class);
        assertRetriedAndFailedMetricTriggered();
    }
}
