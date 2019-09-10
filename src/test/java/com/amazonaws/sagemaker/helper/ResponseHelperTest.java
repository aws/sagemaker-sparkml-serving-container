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

package com.amazonaws.sagemaker.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class ResponseHelperTest {

    private List<Object> dummyResponse = Lists.newArrayList();
    private ResponseHelper responseHelperTest = new ResponseHelper(new ObjectMapper());

    @Before
    public void setup() {
        dummyResponse = Lists.newArrayList(new Integer("1"), new Float("0.2"));
    }

    @Test
    public void testSingleOutput() {
        ResponseEntity<String> outputTest = responseHelperTest.sendResponseForSingleValue("1", "text/csv");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
        Assert.assertEquals(outputTest.getBody(), "1");
    }

    @Test
    public void testSingleJsonlines() {
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForSingleValue("1", "application/jsonlines");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "application/jsonlines");
        Assert.assertEquals(outputTest.getBody(), "1");
    }

    @Test
    public void testSingleOutputNoContentType() {
        ResponseEntity<String> outputTest = responseHelperTest.sendResponseForSingleValue("1", null);
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
        Assert.assertEquals(outputTest.getBody(), "1");
    }

    @Test
    public void testListOutputCsv() throws JsonProcessingException {
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(Collections.singletonList(dummyResponse.iterator()), "text/csv");
        Assert.assertEquals(outputTest.getBody(), "1,0.2");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
    }

    @Test
    public void testListOutputJsonlines() throws JsonProcessingException {
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(Collections.singletonList(dummyResponse.iterator()), "application/jsonlines");
        Assert.assertEquals(outputTest.getBody(), "[{\"features\":[1,0.2]}]");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "application/jsonlines");
    }

    @Test
    public void testTextOutputJsonlines() throws JsonProcessingException {
        dummyResponse = Lists.newArrayList("this", "is", "spark", "ml", "server");
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(Collections.singletonList(dummyResponse.iterator()), "application/jsonlines;data=text");
        Assert.assertEquals(outputTest.getBody(), "[{\"source\":\"this is spark ml server\"}]");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "application/jsonlines");
    }

    @Test
    public void testListOutputInvalidAccept() throws JsonProcessingException {
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(Collections.singletonList(dummyResponse.iterator()), "application/json");
        Assert.assertEquals(outputTest.getBody(), "1,0.2");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
    }

    @Test
    public void testTextOutputInvalidAccept() throws JsonProcessingException {
        dummyResponse = Lists.newArrayList("this", "is", "spark", "ml", "server");
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(Collections.singletonList(dummyResponse.iterator()), "application/json");
        Assert.assertEquals(outputTest.getBody(), "this,is,spark,ml,server");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
    }

}
