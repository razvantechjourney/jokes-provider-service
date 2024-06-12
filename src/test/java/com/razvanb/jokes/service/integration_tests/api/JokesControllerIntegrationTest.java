package com.razvanb.jokes.service.integration_tests.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.razvanb.jokes.service.model.JokeData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class JokesControllerIntegrationTest {
    private final String BASE_URL = "/jokes-service/public";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${app.jokes-limit}")
    private int jokesLimit;

    @Value("${app.default-jokes-count}")
    private int defaultJokesCount;

    @Test
    public void givenStructuredConcurrentRequests_whenGetJokes_thenHandleSuccessfully() throws InterruptedException, ExecutionException {
        int threadCount = 10;
        int jokesCount = 1;

        List<Callable<ResponseEntity<JokeData[]>>> tasks = IntStream.range(0, threadCount)
                .mapToObj(_ -> (Callable<ResponseEntity<JokeData[]>>) () -> {
                    String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes?count=\{jokesCount}";
                    return restTemplate.getForEntity(url, JokeData[].class);
                })
                .toList();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var subtasks = tasks.stream()
                    .map(scope::fork)
                    .collect(Collectors.toList());

            scope.join();
            scope.throwIfFailed(e -> new RuntimeException("Request failed", e));

            for (StructuredTaskScope.Subtask<ResponseEntity<JokeData[]>> subtask : subtasks) {
                ResponseEntity<JokeData[]> response = subtask.get();
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(jokesCount, response.getBody().length);
                assertTrue(Boolean.parseBoolean(response.getHeaders().getFirst("X-Thread-Is-Virtual")));
            }
        }
    }

    @Test
    public void givenConcurrentRequestsExecutedByThreadPerTaskExecutor_whenGetJokes_thenHandleSuccessfully() throws InterruptedException, ExecutionException {
        int threadCount = 3;
        List<Callable<ResponseEntity<JokeData[]>>> tasks = IntStream.range(0, threadCount)
                .mapToObj(_ -> (Callable<ResponseEntity<JokeData[]>>) () -> {
                    String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes";
                    return restTemplate.getForEntity(url, JokeData[].class);
                })
                .toList();

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<ResponseEntity<JokeData[]>>> futures = executorService.invokeAll(tasks);

        for (Future<ResponseEntity<JokeData[]>> future : futures) {
            ResponseEntity<JokeData[]> response = future.get();
            assertThat(response.getStatusCode(), is(HttpStatus.OK));
            assertThat(response.getBody().length, is(defaultJokesCount));
        }

        executorService.shutdown();
    }

    @Test
    public void givenConcurrentRequestsExecutedByThreadPool_whenGetJokes_thenAssertEachRequestIsExecutedByAVirtualThread() throws InterruptedException, ExecutionException, JsonProcessingException {
        int threadCount = 10;
        int jokesCount = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Callable<ResponseEntity<JokeData[]>>> tasks = IntStream.range(0, threadCount)
                .mapToObj(_ -> (Callable<ResponseEntity<JokeData[]>>) () -> {
                    String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes?count=\{jokesCount}";
                    return restTemplate.getForEntity(url, JokeData[].class);
                })
                .collect(Collectors.toList());

        List<Future<ResponseEntity<JokeData[]>>> futures = executorService.invokeAll(tasks);
        for(var future : futures) {
            ResponseEntity<JokeData[]> response = future.get();
            assertThat(response.getStatusCode(), is(HttpStatus.OK));
            assertThat(response.getBody().length, is(jokesCount));
            assertTrue(Boolean.parseBoolean(response.getHeaders().getFirst("X-Thread-Is-Virtual")));
            assertEquals("java.lang.VirtualThread", response.getHeaders().getFirst("X-Thread-Class"));
        }

        executorService.shutdown();
    }

    @Test
    public void givenRequestForDuplicateJokes_whenGetJokes_thenReturnUniqueJokes() {
        String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes";

        ResponseEntity<JokeData[]> response = restTemplate.getForEntity(url, JokeData[].class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        List<JokeData> jokes = List.of(response.getBody());
        assertThat(new HashSet<>(jokes).size(), is(defaultJokesCount));
    }

    @Test
    public void givenNoCountParam_whenGetJokes_thenReturnDefaultNumberOfJokes() {
        String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes";

        ResponseEntity<JokeData[]> response = restTemplate.getForEntity(url, JokeData[].class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        List<JokeData> jokes = List.of(response.getBody());
        assertThat(jokes, hasSize(defaultJokesCount));
    }

    @Test
    public void givenOneJokeRequested_whenGetJokes_thenReturnOneJoke() {
        int jokesCount = 1;
        String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes?count=\{jokesCount}";

        ResponseEntity<List<JokeData>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(jokesCount));
        assertThat(response.getBody().getFirst().id(), notNullValue());
        assertThat(response.getBody().getFirst().type(), notNullValue());
        assertThat(response.getBody().getFirst().setup(), notNullValue());
        assertThat(response.getBody().getFirst().punchline(), notNullValue());
    }

    @Disabled("Running this test will exhaust the maximum number of requests that can be made in a 15-minute interval")
    @Test
    public void givenCountOneHundred_whenGetJokes_thenReturnOneHundredJokes() {
        String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes?count=\{jokesLimit}";
        ResponseEntity<JokeData[]> response = restTemplate.getForEntity(url, JokeData[].class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        List<JokeData> jokes = List.of(response.getBody());
        assertThat(jokes, hasSize(100));
        for (JokeData joke : jokes) {
            assertThat(joke.id(), notNullValue());
            assertThat(joke.type(), notNullValue());
            assertThat(joke.setup(), notNullValue());
            assertThat(joke.punchline(), notNullValue());
        }
    }

    @Test
    void givenCountIsAboveLimit_whenGetJokes_thenBadRequest() {
        String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes?count=\{jokesLimit + 1}";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("You can get no more than 100 jokes at a time.", response.getBody());
    }

    @Test
    public void givenRequest_whenGetJokes_thenReturnCorrectContentType() {
        String url = STR."http://localhost:\{port}\{BASE_URL}/v1/jokes";

        ResponseEntity<JokeData[]> response = restTemplate.getForEntity(url, JokeData[].class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON));
    }
}
