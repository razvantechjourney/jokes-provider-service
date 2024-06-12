package com.razvanb.jokes.service.integration_tests.service;

import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.model.JokeData;
import com.razvanb.jokes.service.model.properties.JokeLimitsProperties;
import com.razvanb.jokes.service.service.JokesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
public class JokesServiceIntegrationTest {
    @Autowired
    private JokesService service;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JokeLimitsProperties properties;

    @BeforeEach
    void dbCleanup() {
        mongoTemplate.remove(new Query(), JokeDocument.class);
    }

    @Test
    void givenNoCount_whenGetAndPersistJokes_thenJokesPersistedAndReturnedCorrectly() throws Throwable {
        List<JokeData> jokes = service.getAndPersistJokes(null);

        assertEquals(properties.defaultJokesCount(), jokes.size());

        var jokesSortedById = jokes.stream().sorted(Comparator.comparing(JokeData::id)).toList();
        List<JokeDocument> documentsSortedByExternalId = new ArrayList<>();
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            var documents = mongoTemplate.findAll(JokeDocument.class);
            assertEquals(properties.defaultJokesCount(), documents.size());

            documentsSortedByExternalId.addAll(
                    documents.stream().sorted(Comparator.comparing(JokeDocument::externalId)).toList()
            );
        });

        for (int i = 0; i < jokesSortedById.size(); i++) {
            assertFalse(documentsSortedByExternalId.get(i).id().isEmpty());
            assertEquals(jokesSortedById.get(i).id(), documentsSortedByExternalId.get(i).externalId());
            assertEquals(jokesSortedById.get(i).type(), documentsSortedByExternalId.get(i).type());
            assertEquals(jokesSortedById.get(i).setup(), documentsSortedByExternalId.get(i).setup());
            assertEquals(jokesSortedById.get(i).punchline(), documentsSortedByExternalId.get(i).punchline());
        }
    }

    @Test
    void givenNoTenMultipleCount_whenGetAndPersistJokes_thenPersistAndReturnAllJokes() throws Throwable {
        int jokesCount = 11;
        List<JokeData> jokes = service.getAndPersistJokes(jokesCount);

        assertEquals(jokesCount, jokes.size());
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            var documents = mongoTemplate.findAll(JokeDocument.class);
            assertEquals(jokesCount, documents.size());
        });
    }
}
