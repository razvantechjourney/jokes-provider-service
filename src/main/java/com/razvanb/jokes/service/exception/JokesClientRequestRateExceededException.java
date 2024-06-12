package com.razvanb.jokes.service.exception;

public class JokesClientRequestRateExceededException extends RuntimeException {
    private final int statusCode;

    public JokesClientRequestRateExceededException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return STR."""
                JokesClientRequestRateExceededException {
                statusCode= \{statusCode}
                message= \{getMessage()}
                }
                """;
    }
}