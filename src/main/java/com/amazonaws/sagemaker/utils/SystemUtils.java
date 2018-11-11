package com.amazonaws.sagemaker.utils;

/**
 * Utility class for dealing with System or Environment related functionalities. These methofs are moved to this class
 * so that they can be easily mocked out by PowerMockito.mockStatic while testing the actual classes.
 */
public class SystemUtils {

    /**
     * Computes the number of threads to use based on number of available processors in the host
     *
     * @param coreToThreadRatio, the multiplicative factor per core
     * @return coreToThreadRatio multiplied by available cores in the host
     */
    public static int getNumberOfThreads(final Integer coreToThreadRatio) {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        return coreToThreadRatio * numberOfCores;
    }

    /**
     * Retrieves environment variable pertaining to a key
     *
     * @param key, the environment variable key
     * @return the value corresponding to the key from environment settings
     */
    public static String getEnvironmentVariable(final String key) {
        return System.getenv(key);
    }

}
