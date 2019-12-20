package com.dall.error;

public class MapsException extends RuntimeException {
    public MapsException(String errorMessage) {
        super(errorMessage);
    }
}
