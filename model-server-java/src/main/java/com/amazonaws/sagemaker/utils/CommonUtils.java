package com.amazonaws.sagemaker.utils;

import org.springframework.http.ResponseEntity;

public class CommonUtils {

    private static final int CORE_TO_THREAD_RATIO = 10;

    public static int getNumberOfThreads() {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        return CORE_TO_THREAD_RATIO * numberOfCores;
    }

    public static ResponseEntity<String> throwBadRequest(final String errorMessage) {
        return ResponseEntity.badRequest().body(errorMessage);
    }

}
