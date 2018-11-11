package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class JsonlinesTextOutputTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStandardJsonOutputObjectCreation() {
        JsonlinesTextOutput jsonlinesTextOutputTest = new JsonlinesTextOutput("this is spark ml server");
        Assert.assertEquals(jsonlinesTextOutputTest.getSource(), "this is spark ml server");
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputPassedToConstructor() {
        new JsonlinesTextOutput(null);
    }

    @Test
    public void testParseStandardJsonOutput() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("text_json_out.json"), "UTF-8");
        JsonlinesTextOutput sjo = mapper.readValue(inputJson, JsonlinesTextOutput.class);
        Assert.assertEquals(sjo.getSource(), "this is spark ml server");
    }

}
