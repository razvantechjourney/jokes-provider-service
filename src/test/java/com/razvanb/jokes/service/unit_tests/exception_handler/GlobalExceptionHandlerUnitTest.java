package com.razvanb.jokes.service.unit_tests.exception_handler;

import com.razvanb.jokes.service.exception.JokesCountIsLessThanOneException;
import com.razvanb.jokes.service.exception.JokesLimitExceededException;
import com.razvanb.jokes.service.exception_handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerUnitTest {
    @Value("${app.jokes-limit}")
    private int jokesLimit;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(jokesLimit);

    @Test
     void givenCountLessThanOne_whenGetStatistics_thenReturnBadRequest() {
        JokesCountIsLessThanOneException exception = new JokesCountIsLessThanOneException("Requested less than one joke.");

        ResponseEntity<String> response = handler.handleNegativeJokesCountException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Requested less than one joke.", response.getBody());
    }

    @Test
    void givenCountExceedUpperLimit_whenGetStatistics_thenReturnBadRequest() {
        JokesLimitExceededException exception = new JokesLimitExceededException("Requested crypto is not supported.");

        ResponseEntity<String> response = handler.handleJokesLimitExceededException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(STR."You can get no more than \{jokesLimit} jokes at a time.", response.getBody());
    }
}