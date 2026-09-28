package com.example.lending.rulesengine;

public class CollateralRequiredException extends RuntimeException {
    public CollateralRequiredException(String message) {
        super(message);
    }
}
