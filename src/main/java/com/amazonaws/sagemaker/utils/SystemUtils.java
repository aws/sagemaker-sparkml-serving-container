/*
 *  Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 *  A copy of the License is located at
 *
 *      http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */

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
