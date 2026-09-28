package com.example.lending.rulesengine;

public class NomineeRequiredException extends RuntimeException {
    public NomineeRequiredException(String message) {
        super(message);
    }
}
