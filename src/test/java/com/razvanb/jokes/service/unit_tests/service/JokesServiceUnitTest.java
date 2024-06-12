package com.razvanb.jokes.service.unit_tests.service;

import com.razvanb.jokes.service.client.JokesClient;
import com.razvanb.jokes.service.domain.JokeDocument;
import com.razvanb.jokes.service.exception.JokesCountIsLessThanOneException;
import com.razvanb.jokes.service.exception.JokesLimitExceededException;
import com.razvanb.jokes.service.mapper.JokeMapper;
import com.razvanb.jokes.service.model.JokeData;
import com.razvanb.jokes.service.model.properties.JokeLimitsProperties;
import com.razvanb.jokes.service.repository.JokesRepository;
import com.razvanb.jokes.service.service.JokesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JokesServiceUnitTest {
    @InjectMocks
    JokesService service;

    @Mock
    private JokesRepository repository;

    @Mock
    private JokesClient client;

    @Mock
    private JokeMapper mapper;

    @Mock
    private JokeLimitsProperties properties;

    @Captor
    ArgumentCaptor<List<JokeDocument>> jokeDocumentListCaptor;

    @BeforeEach
    public void setUp() {
        when(properties.jokesLimit()).thenReturn(100);
    }

    @Test
    void givenNoCount_whenGetAndPersistJokes_thenPersistAndReturnDefaultNumberOfJokes() throws Throwable {
        when(properties.defaultJokesCount()).thenReturn(5);
        when(properties.batchSize()).thenReturn(10);
        when(client.getRandomJoke()).thenReturn(new JokeData(1, "type", "setup", "punchline"));

        List<JokeData> jokes = service.getAndPersistJokes(null);

        assertEquals(5, jokes.size());
        verify(client, times(5)).getRandomJoke();
        verify(mapper, times(5)).mapToDocument(any(JokeData.class));
        verify(repository, times(1)).saveAll(jokeDocumentListCaptor.capture());
        var jokeDocuments = jokes.stream().map(mapper::mapToDocument).toList();
        assertEquals(jokeDocuments, jokeDocumentListCaptor.getValue());
    }

    @Test
    void givenCountIs1_whenGetAndPersistJokes_thenPersistAndReturnOneJoke() throws Throwable {
        when(properties.batchSize()).thenReturn(10);
        when(client.getRandomJoke()).thenReturn(new JokeData(1, "type", "setup", "punchline"));

        List<JokeData> jokes = service.getAndPersistJokes(1);

        assertEquals(1, jokes.size());
        verify(client, times(1)).getRandomJoke();
        verify(mapper, times(1)).mapToDocument(any(JokeData.class));
        verify(repository, times(1)).saveAll(jokeDocumentListCaptor.capture());
        var jokeDocuments = jokes.stream().map(mapper::mapToDocument).toList();
        assertEquals(jokeDocuments, jokeDocumentListCaptor.getValue());
    }

    @Test
    void givenCountIs100_whenGetAndPersistJokes_thenPersistAndReturnAllJokes() throws Throwable {
        when(properties.batchSize()).thenReturn(10);
        when(client.getRandomJoke()).thenReturn(new JokeData(1, "type", "setup", "punchline"));

        List<JokeData> jokes = service.getAndPersistJokes(100);

        assertEquals(100, jokes.size());
        verify(client, times(100)).getRandomJoke();
        verify(mapper, times(100)).mapToDocument(any(JokeData.class));
        verify(repository, times(1)).saveAll(jokeDocumentListCaptor.capture());
        var jokeDocuments = jokes.stream().map(mapper::mapToDocument).toList();
        assertEquals(jokeDocuments, jokeDocumentListCaptor.getValue());
    }

    @Test
    void givenCountExceedUpperLimit_whenGetAndPersistJokes_throwJokesLimitExceededException() {
        int jokesCount = 1 + properties.jokesLimit();
        JokesLimitExceededException exception = assertThrows(JokesLimitExceededException.class, () ->
                service.getAndPersistJokes(jokesCount));

        assertEquals(STR."Requested \{jokesCount} jokes but the limit is \{properties.jokesLimit()}", exception.getMessage());
        verify(client, never()).getRandomJoke();
        verify(mapper, never()).mapToDocument(any(JokeData.class));
        verify(repository, never()).saveAll(jokeDocumentListCaptor.capture());
    }

    @Test
    void givenCountIsZero_whenGetAndPersistJokes_throwJokesCountIsLessThanOneException() {
        int jokesCount = 0;
        JokesCountIsLessThanOneException exception = assertThrows(JokesCountIsLessThanOneException.class, () ->
                service.getAndPersistJokes(jokesCount));

        assertEquals(STR."Less than one joke requested: \{jokesCount}.", exception.getMessage());
        verify(client, never()).getRandomJoke();
        verify(mapper, never()).mapToDocument(any(JokeData.class));
        verify(repository, never()).saveAll(jokeDocumentListCaptor.capture());
    }

    @Test
    void givenNegativeCount_whenGetAndPersistJokes_throwJokesCountIsLessThanOneException() {
        int jokesCount = -2;
        JokesCountIsLessThanOneException exception = assertThrows(JokesCountIsLessThanOneException.class, () ->
                service.getAndPersistJokes(jokesCount));

        assertEquals(STR."Less than one joke requested: \{jokesCount}.", exception.getMessage());
        verify(client, never()).getRandomJoke();
        verify(mapper, never()).mapToDocument(any(JokeData.class));
        verify(repository, never()).saveAll(jokeDocumentListCaptor.capture());
    }
}
