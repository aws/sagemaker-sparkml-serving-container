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
