package com.razvanb.jokes.service.exception;

public class JokesLimitExceededException extends RuntimeException {
    public JokesLimitExceededException(String msg) {
        super(msg);
    }
}
