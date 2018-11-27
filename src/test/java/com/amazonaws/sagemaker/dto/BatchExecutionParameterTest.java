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

package com.amazonaws.sagemaker.dto;

import org.junit.Assert;
import org.junit.Test;

public class BatchExecutionParameterTest {

    @Test
    public void testBatchExecutionParameterObjectCreation() {
        BatchExecutionParameter testBatchExecution = new BatchExecutionParameter(1, "SINGLE_RECORD", 5);
        Assert.assertEquals(testBatchExecution.getBatchStrategy(), "SINGLE_RECORD");
        Assert.assertEquals(new Integer("1"), testBatchExecution.getMaxConcurrentTransforms());
        Assert.assertEquals(new Integer("5"), testBatchExecution.getMaxPayloadInMB());
    }

    @Test(expected = NullPointerException.class)
    public void testNullBatchStrategyPassedToConstructor() {
        new BatchExecutionParameter(1, null, 5);
    }

    @Test(expected = NullPointerException.class)
    public void testNullConcurrentTransformsPassedToConstructor() {
        new BatchExecutionParameter(null, "SINGLE_RECORD", 5);
    }

    @Test(expected = NullPointerException.class)
    public void testNullMaxPayloadPassedToConstructor() {
        new BatchExecutionParameter(1, "SINGLE_RECORD", null);
    }

}
