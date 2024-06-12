package com.razvanb.jokes.service.integration_tests.client.retry;

import com.razvanb.jokes.service.integration_tests.client.JokesClientBaseIntegrationTest;
import com.razvanb.jokes.service.model.JokeData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"app.resilience.retry.retry-attempts=2"})
public class JokesClientIntegrationTestTwoRetries extends JokesClientBaseIntegrationTest {


    @Test
    public void givenRetryAttemptsIsSetToN_whenGettingJokeThrowsNretryableExceptions_thenRetriesExactlyNtimesThenException() {
        when(restTemplate.getForObject(jokeApiUrl, JokeData.class))
                .thenThrow(new ResourceAccessException("I/O Exception"))
                .thenThrow(new ResourceAccessException("I/O Exception"))
                .thenReturn(new JokeData(1, "test", "setup", "punchline"));

        assertThrows(ResourceAccessException.class, () -> jokesClient.getRandomJoke());

        verify(restTemplate, times(2)).getForObject(jokeApiUrl, JokeData.class);
        assertRetriedAndFailedMetricTriggered();
    }
}
