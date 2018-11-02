package com.amazonaws.sagemaker.utils;

import org.springframework.http.ResponseEntity;

public class ErrorResponseUtils {

    public static ResponseEntity<String> throwBadRequest(final String errorMessage) {
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
