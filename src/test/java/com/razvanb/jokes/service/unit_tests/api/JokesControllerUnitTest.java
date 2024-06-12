package com.razvanb.jokes.service.unit_tests.api;

import com.razvanb.jokes.service.api.JokesController;
import com.razvanb.jokes.service.model.JokeData;
import com.razvanb.jokes.service.service.JokesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JokesControllerUnitTest {
    @InjectMocks
    private JokesController controller;

    @Mock
    private JokesService service;

    @Test
    void givenNoCountSpecified_whenGetJokes_thenGetAndPersistJokesIsCalled() throws Throwable {
        var mockedJokes = List.of(
                new JokeData(1, "programming", "No joke!", "Got it."),
                new JokeData(2, "programming", "No joke!", "Got it."),
                new JokeData(3, "programming", "No joke!", "Got it."),
                new JokeData(4, "programming", "No joke!", "Got it.")
        );
        when(service.getAndPersistJokes(null)).thenReturn(mockedJokes);

        ResponseEntity<List<JokeData>> response = controller.getJokes(null);

        verify(service).getAndPersistJokes(null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockedJokes, response.getBody());
    }
}
