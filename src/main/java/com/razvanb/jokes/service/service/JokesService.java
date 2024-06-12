package com.razvanb.jokes.service.service;

import com.razvanb.jokes.service.client.JokesClient;
import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.exception.JokesCountIsLessThanOneException;
import com.razvanb.jokes.service.exception.JokesLimitExceededException;
import com.razvanb.jokes.service.mapper.JokeMapper;
import com.razvanb.jokes.service.model.JokeData;
import com.razvanb.jokes.service.model.properties.JokeLimitsProperties;
import com.razvanb.jokes.service.repository.JokesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

@Service
public class JokesService {
    private final JokesRepository repository;
    private final JokesClient jokesClient;
    private final JokeMapper jokeMapper;
    private final JokeLimitsProperties properties;

    public JokesService(JokesRepository repository, JokesClient jokesClient, JokeMapper jokeMapper, JokeLimitsProperties properties) {
        this.repository = repository;
        this.jokesClient = jokesClient;
        this.jokeMapper = jokeMapper;
        this.properties = properties;
    }

    public List<JokeData> getAndPersistJokes(Integer count) throws Throwable {
        if (null == count) {
            count = properties.defaultJokesCount();
        }
        validateJokesCount(count);
        List<JokeData> jokes = getJokesInBatches(count);
        saveJokes(jokes);

        return jokes;
    }

    private List<JokeData> getJokesInBatches(Integer count) throws Throwable {
        List<StructuredTaskScope.Subtask<List<JokeData>>> getJokesBatchSubtasks = new ArrayList<>();
        int batchesCount = computeBatchCount(count);
        int jokesRequestedSoFar = 0;

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < batchesCount; i++) {
                int crtBatchSize = computeBatchSize(count, jokesRequestedSoFar);
                getJokesBatchSubtasks.add(scope.fork(() -> getJokesBatch(crtBatchSize)));
                jokesRequestedSoFar += crtBatchSize;
            }
            scope.join();
            scope.throwIfFailed(e -> e);

            return getJokesBatchSubtasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .flatMap(List::stream)
                    .toList();
        }
    }

    private List<JokeData> getJokesBatch(int batchSize) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<JokeData>> fetchRandomJokeSubtasks = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                fetchRandomJokeSubtasks.add(scope.fork(() -> jokesClient.getRandomJoke()));
            }

            scope.join();
            scope.throwIfFailed();

            return fetchRandomJokeSubtasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();
        }
    }

    private int computeBatchCount(int count) {
        return (int) Math.ceil((double) count / properties.batchSize());
    }

    private int computeBatchSize(Integer count, int jokesRequestedSoFar) {
        return Math.min(properties.batchSize(), count - jokesRequestedSoFar);
    }

    private void saveJokes(List<JokeData> jokes) {
        List<JokeDocument> documents = jokes.stream()
                .map(jokeMapper::mapToDocument)
                .toList();

        Thread.ofVirtual().start(() -> repository.saveAll(documents));
    }

    private void validateJokesCount(Integer count) {
        if (count > properties.jokesLimit()) {
            throw new JokesLimitExceededException(STR."Requested \{count} jokes but the limit is \{properties.jokesLimit()}");
        }
        if (count < 1) {
            throw new JokesCountIsLessThanOneException(STR."Less than one joke requested: \{count}.");
        }
    }
}
