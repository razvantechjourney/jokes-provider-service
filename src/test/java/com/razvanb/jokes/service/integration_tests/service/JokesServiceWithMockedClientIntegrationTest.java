package com.razvanb.jokes.service.integration_tests.service;

import com.razvanb.jokes.service.client.JokesClient;
import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.model.JokeData;
import com.razvanb.jokes.service.model.properties.JokeLimitsProperties;
import com.razvanb.jokes.service.service.JokesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "app.resilience.rate.requestsLimit=100000",
        "app.resilience.rate.limitForSeconds=1"
})
@ActiveProfiles("test")
@Disabled("As the application has a rate limiter configured, in production this application will never execute 100000 requests")
public class JokesServiceWithMockedClientIntegrationTest {
    @Autowired
    private JokesService service;

    @SpyBean
    private JokeLimitsProperties properties;

    @SpyBean
    private JokesClient client;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        mongoTemplate.remove(new Query(), JokeDocument.class);
        when(client.getRandomJoke()).thenReturn(new JokeData(1, "type", "setup", "punchline"));
    }

    @Test
    public void givenLargeJokesCount_whenGetAndPersistJokes_thenTestPerformance() throws Throwable {
        int count = 100_000;
        when(properties.jokesLimit()).thenReturn(count);

        long startTime = System.currentTimeMillis();
        List<JokeData> jokes = service.getAndPersistJokes(count);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(count, jokes.size());
        long expectedDuration = 50000;
        assertTrue(duration < expectedDuration,
                STR."Performance test failed: duration was \{duration} milliseconds, expected was \{expectedDuration}");
    }

    @Test
    public void givenJokesCount_whenGetAndPersistJokes_thenTestLoad() throws Throwable {
        int count = 100;
        int concurrentVirtualThreads = 1_000;
        when(properties.jokesLimit()).thenReturn(count);

        List<StructuredTaskScope.Subtask<List<JokeData>>> subtasks = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < concurrentVirtualThreads; i++) {
                subtasks.add(scope.fork(() -> {
                    try {
                        return service.getAndPersistJokes(count);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
            scope.join();
            scope.throwIfFailed(e -> e);

            List<JokeData> jokes = subtasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .flatMap(List::stream)
                    .toList();
            long duration = System.currentTimeMillis() - startTime;

            assertEquals(concurrentVirtualThreads * count, jokes.size());

            long expectedDuration = 100000;
            assertTrue(duration < expectedDuration,
                    STR."Load test failed: duration was \{duration} milliseconds, expected was \{expectedDuration}");
            var persistedDocuments = mongoTemplate.findAll(JokeDocument.class);
            assertEquals(persistedDocuments.size(), count * concurrentVirtualThreads);
        }
    }
}