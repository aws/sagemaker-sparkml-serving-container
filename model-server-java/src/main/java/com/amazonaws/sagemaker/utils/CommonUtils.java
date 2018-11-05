package com.amazonaws.sagemaker.utils;

public class CommonUtils {

    public static int getNumberOfThreads(final Integer coreToThreadRatio) {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        return coreToThreadRatio * numberOfCores;
    }

}
