package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StandardJsonlinesOutputTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testStandardJsonOutputObjectCreation() {
        List<Object> featureList = Lists.newArrayList(new Integer("1"), new Double("2.0"), "3");
        StandardJsonlinesOutput standardJsonlinesOutputTest = new StandardJsonlinesOutput(featureList);
        Assert.assertNotNull(standardJsonlinesOutputTest.getFeatures());
        Assert.assertTrue(standardJsonlinesOutputTest.getFeatures().get(0) instanceof Integer);
        Assert.assertTrue(standardJsonlinesOutputTest.getFeatures().get(1) instanceof Double);
        Assert.assertTrue(standardJsonlinesOutputTest.getFeatures().get(2) instanceof String);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputPassedToConstructor() {
        new StandardJsonlinesOutput(null);
    }

    @Test
    public void testParseStandardJsonOutput() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("standard_json_out.json"), "UTF-8");
        StandardJsonlinesOutput sjo = mapper.readValue(inputJson, StandardJsonlinesOutput.class);
        Assert.assertEquals(sjo.getFeatures(), Lists.newArrayList(1.0,2.0,4.0));
    }
}
