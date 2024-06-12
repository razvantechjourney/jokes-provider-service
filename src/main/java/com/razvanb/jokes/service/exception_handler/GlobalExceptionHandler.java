package com.razvanb.jokes.service.exception_handler;

import com.razvanb.jokes.service.exception.JokesClientRequestRateExceededException;
import com.razvanb.jokes.service.exception.JokesCountIsLessThanOneException;
import com.razvanb.jokes.service.exception.JokesLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.concurrent.ExecutionException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final int jokesLimit;

    public GlobalExceptionHandler(@Value("${app.jokes-limit}") int jokesLimit) {
        this.jokesLimit = jokesLimit;
    }

    @ExceptionHandler(JokesCountIsLessThanOneException.class)
    public ResponseEntity<String> handleNegativeJokesCountException(JokesCountIsLessThanOneException e) {
        LOGGER.error("Requested less than one joke: {}.", e.getMessage());
        return ResponseEntity.badRequest().body("Requested less than one joke.");
    }

    @ExceptionHandler(JokesLimitExceededException.class)
    public ResponseEntity<String> handleJokesLimitExceededException(JokesLimitExceededException e) {
        LOGGER.error("The limit of jokes number was exceeded: {}.", e.getMessage());
        return ResponseEntity.badRequest().body(STR."You can get no more than \{jokesLimit} jokes at a time.");
    }

    @ExceptionHandler(JokesClientRequestRateExceededException.class)
    public ResponseEntity<String> handleJokesClientRequestRateExceededException(JokesClientRequestRateExceededException e) {
        LOGGER.error("Client rate limit exceeded: {}.", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> handleExecutionException(ExecutionException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof JokesClientRequestRateExceededException e) {
            handleJokesClientRequestRateExceededException(e);
        }
        if (cause instanceof HttpClientErrorException.TooManyRequests e) {
            LOGGER.error("Client rate limit exceeded: {}.", e.getMessage());
            return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.TOO_MANY_REQUESTS);
        }
        return handleException(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        LOGGER.error("Unexpected error occurred: {}", e.getMessage());
        return ResponseEntity.internalServerError().body("An unexpected error occurred.");
    }

}
