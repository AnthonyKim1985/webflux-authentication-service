package com.example.auth.exception;

/**
 * @author Anthony Jinhyuk Kim
 * @version 1.0.0
 * @since 2018-09-20
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
