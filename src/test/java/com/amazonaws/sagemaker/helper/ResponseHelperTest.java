package com.amazonaws.sagemaker.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
            .sendResponseForList(dummyResponse.iterator(), "text/csv");
        Assert.assertEquals(outputTest.getBody(), "1,0.2");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
    }

    @Test
    public void testListOutputJsonlines() throws JsonProcessingException {
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(dummyResponse.iterator(), "application/jsonlines");
        Assert.assertEquals(outputTest.getBody(), "{\"features\":[1,0.2]}");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "application/jsonlines");
    }

    @Test
    public void testTextOutputJsonlines() throws JsonProcessingException {
        dummyResponse = Lists.newArrayList("this", "is", "spark", "ml", "server");
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(dummyResponse.iterator(), "application/jsonlines;data=text");
        Assert.assertEquals(outputTest.getBody(), "{\"source\":\"this is spark ml server\"}");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "application/jsonlines");
    }

    @Test
    public void testListOutputInvalidAccept() throws JsonProcessingException {
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(dummyResponse.iterator(), "application/json");
        Assert.assertEquals(outputTest.getBody(), "1,0.2");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
    }

    @Test
    public void testTextOutputInvalidAccept() throws JsonProcessingException {
        dummyResponse = Lists.newArrayList("this", "is", "spark", "ml", "server");
        ResponseEntity<String> outputTest = responseHelperTest
            .sendResponseForList(dummyResponse.iterator(), "application/json");
        Assert.assertEquals(outputTest.getBody(), "this,is,spark,ml,server");
        Assert.assertEquals(Objects.requireNonNull(outputTest.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0),
            "text/csv");
    }

}
