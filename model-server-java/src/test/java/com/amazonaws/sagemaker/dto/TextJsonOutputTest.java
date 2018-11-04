package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TextJsonOutputTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testStandardJsonOutputObjectCreation() {
        TextJsonOutput textJsonOutputTest = new TextJsonOutput("this is spark ml server");
        Assert.assertEquals(textJsonOutputTest.getSource(), "this is spark ml server");
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputPassedToConstructor() {
        new TextJsonOutput(null);
    }

    @Test
    public void testParseStandardJsonOutput() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("text_json_out.json"), "UTF-8");
        TextJsonOutput sjo = mapper.readValue(inputJson, TextJsonOutput.class);
        Assert.assertEquals(sjo.getSource(), "this is spark ml server");
    }

}
