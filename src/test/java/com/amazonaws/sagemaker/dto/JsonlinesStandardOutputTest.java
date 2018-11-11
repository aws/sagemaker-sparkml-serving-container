package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class JsonlinesStandardOutputTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStandardJsonOutputObjectCreation() {
        List<Object> featureList = Lists.newArrayList(new Integer("1"), new Double("2.0"), "3");
        JsonlinesStandardOutput jsonlinesStandardOutputTest = new JsonlinesStandardOutput(featureList);
        Assert.assertNotNull(jsonlinesStandardOutputTest.getFeatures());
        Assert.assertTrue(jsonlinesStandardOutputTest.getFeatures().get(0) instanceof Integer);
        Assert.assertTrue(jsonlinesStandardOutputTest.getFeatures().get(1) instanceof Double);
        Assert.assertTrue(jsonlinesStandardOutputTest.getFeatures().get(2) instanceof String);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputPassedToConstructor() {
        new JsonlinesStandardOutput(null);
    }

    @Test
    public void testParseStandardJsonOutput() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("standard_json_out.json"), "UTF-8");
        JsonlinesStandardOutput sjo = mapper.readValue(inputJson, JsonlinesStandardOutput.class);
        Assert.assertEquals(sjo.getFeatures(), Lists.newArrayList(1.0, 2.0, 4.0));
    }
}
