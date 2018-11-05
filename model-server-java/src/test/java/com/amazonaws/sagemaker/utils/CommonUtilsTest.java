package com.amazonaws.sagemaker.utils;

import org.junit.Assert;
import org.junit.Test;

public class CommonUtilsTest {

    @Test
    public void testGetNumberOfThreads() {
        Assert.assertEquals(2 * Runtime.getRuntime().availableProcessors(), CommonUtils.getNumberOfThreads(2));
    }

}
