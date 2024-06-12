package com.razvanb.jokes.service.integration_tests.client.retry;

import com.razvanb.jokes.service.integration_tests.client.JokesClientBaseIntegrationTest;
import com.razvanb.jokes.service.model.JokeData;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


public class JokesClientRetryIntegrationTest extends JokesClientBaseIntegrationTest {

    @Test
    public void givenTwoRetryableExceptionThrownFollowedBySuccessResponse_whenGetJokeFromExternalApi_thenRetry2TimesThenSuccess() {
        when(restTemplate.getForObject(jokeApiUrl, JokeData.class))
                .thenThrow(new ResourceAccessException("I/O Exception"))
                .thenThrow(new ResourceAccessException("I/O Exception"))
                .thenReturn(new JokeData(1, "test", "setup", "punchline"));

        JokeData jokeData = jokesClient.getRandomJoke();

        verify(restTemplate, times(3)).getForObject(jokeApiUrl, JokeData.class);
        assertNotNull(jokeData);
        assertEquals(1, jokeData.id());
        assertEquals("test", jokeData.type());
        assertEquals("setup", jokeData.setup());
        assertEquals("punchline", jokeData.punchline());
        assertRetriedAndSucceededMetricTriggered();
        assertCustomRetryMetricCountEquals(2);
    }
}
