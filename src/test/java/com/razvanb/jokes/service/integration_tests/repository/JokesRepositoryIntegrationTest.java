package com.razvanb.jokes.service.integration_tests.repository;

import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.repository.JokesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
@ActiveProfiles("test")
@Import(JokesRepository.class)
@TestPropertySource(locations = "classpath:application-test.yml")
public class JokesRepositoryIntegrationTest {
    @Autowired
    private JokesRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setUp() {
        mongoTemplate.dropCollection(JokeDocument.class);
    }

    @Test
    void given100JokeDocuments_whenSaveAll_thenPersistAllAsBatch() {
        List<JokeDocument> jokeDocuments = IntStream.range(0, 100)
                .mapToObj(i -> new JokeDocument("id" + i, i, "type" + i, "setup" + i, "punchline" + i))
                .toList();
        repository.saveAll(jokeDocuments);

        var documents = mongoTemplate.findAll(JokeDocument.class);
        assertEquals(documents.size(), jokeDocuments.size());
    }
}
