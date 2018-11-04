package com.amazonaws.sagemaker.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CommonUtilsTest {

    @Test
    public void testGetNumberOfThreads() {
        Assert.assertEquals(2 * Runtime.getRuntime().availableProcessors(), CommonUtils.getNumberOfThreads(2));
    }

    @Test
    public void testThrowBadRequest() {
        ResponseEntity badRequest = CommonUtils.throwBadRequest("this is bad request");
        Assert.assertEquals(badRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(badRequest.getBody(), "this is bad request");
    }
}
