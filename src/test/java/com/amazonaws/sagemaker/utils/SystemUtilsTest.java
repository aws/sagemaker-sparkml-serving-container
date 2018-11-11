package com.amazonaws.sagemaker.utils;

import org.junit.Assert;
import org.junit.Test;

public class SystemUtilsTest {

    @Test
    public void testGetNumberOfThreads() {
        Assert.assertEquals(2 * Runtime.getRuntime().availableProcessors(), SystemUtils.getNumberOfThreads(2));
    }

}
