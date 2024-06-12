package com.razvanb.jokes.service.exception;

public class JokesCountIsLessThanOneException extends RuntimeException {
    public JokesCountIsLessThanOneException(String msg) {
        super(msg);
    }
}
