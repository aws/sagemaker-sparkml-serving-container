package com.amazonaws.sagemaker.utils;

import org.springframework.http.ResponseEntity;

public class CommonUtils {

    public static int getNumberOfThreads(final Integer coreToThreadRatio) {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        return coreToThreadRatio * numberOfCores;
    }

    public static ResponseEntity<String> throwBadRequest(final String errorMessage) {
        return ResponseEntity.badRequest().body(errorMessage);
    }

}
