package com.razvanb.jokes.service.unit_tests.repository;

import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.repository.JokesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JokesRepositoryUnitTest {
    @InjectMocks
    JokesRepository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void given100JokeDocuments_whenSaveAll_thenPersistAllAsBatch() {
        List<JokeDocument> jokeDocuments = IntStream.range(0, 100)
                .mapToObj(i -> new JokeDocument("id" + i, i, "type" + i, "setup" + i, "punchline" + i))
                .toList();
        repository.saveAll(jokeDocuments);

        verify(mongoTemplate).insertAll(jokeDocuments);
    }
}
