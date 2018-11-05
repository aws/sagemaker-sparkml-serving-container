package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SageMakerRequestObjectTest {

    private List<SingleColumn> input;
    private SingleColumn output;
    private ObjectMapper mapper;

    @Before
    public void setup() {
        input = Lists.newArrayList(new SingleColumn("test_name_1", "test_type_1", "test_struct_1", "test_val_1"),
            new SingleColumn("test_name_2", "test_type_2", "test_struct_2", "test_val_2"));

        output = new SingleColumn("out_name", "out_type", "out_struct", "out_val");

        mapper = new ObjectMapper();
    }

    @Test
    public void testSageMakerRequestObjectCreation() {

        SageMakerRequestObject sageMakerRequestObjectTest = new SageMakerRequestObject(input, output);
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().size(), 2);
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(0).getName(), "test_name_1");
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(0).getType(), "test_type_1");
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(0).getStructure(), "test_struct_1");
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(0).getVal(), "test_val_1");

        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(1).getName(), "test_name_2");
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(1).getType(), "test_type_2");
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(1).getStructure(), "test_struct_2");
        Assert.assertEquals(sageMakerRequestObjectTest.getInput().get(1).getVal(), "test_val_2");

        Assert.assertEquals(sageMakerRequestObjectTest.getOutput().getName(), "out_name");
        Assert.assertEquals(sageMakerRequestObjectTest.getOutput().getType(), "out_type");
        Assert.assertEquals(sageMakerRequestObjectTest.getOutput().getStructure(), "out_struct");
        Assert.assertEquals(sageMakerRequestObjectTest.getOutput().getVal(), "out_val");
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputPassedToConstructor() {
        new SageMakerRequestObject(null, output);
    }

    @Test(expected = NullPointerException.class)
    public void testNullOutputPassedToConstructor() {
        new SageMakerRequestObject(input, null);
    }

    @Test
    public void testParseInputJson_1() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("sagemaker_input_1.json"), "UTF-8");
        SageMakerRequestObject sro = mapper.readValue(inputJson, SageMakerRequestObject.class);
        Assert.assertEquals(sro.getInput().size(), 3);
        Assert.assertEquals(sro.getInput().get(0).getName(), "name_1");
        Assert.assertEquals(sro.getInput().get(1).getName(), "name_2");
        Assert.assertEquals(sro.getInput().get(2).getName(), "name_3");
        Assert.assertEquals(sro.getInput().get(0).getVal(), new Integer("1"));
        Assert.assertEquals(sro.getInput().get(1).getVal(), "C");
        Assert.assertEquals(sro.getInput().get(2).getVal(), new Double("38.0"));
        Assert.assertEquals(sro.getInput().get(0).getType(), "int");
        Assert.assertEquals(sro.getInput().get(1).getType(), "string");
        Assert.assertEquals(sro.getInput().get(2).getType(), "double");
        Assert.assertEquals(sro.getInput().get(0).getStructure(), "basic");
        Assert.assertEquals(sro.getInput().get(1).getStructure(), "basic");
        Assert.assertEquals(sro.getInput().get(2).getStructure(), "basic");
        Assert.assertEquals(sro.getOutput().getName(), "features");
        Assert.assertEquals(sro.getOutput().getType(), "double");
    }

    @Test
    public void testParseInputJson_2() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("sagemaker_input_2.json"), "UTF-8");
        SageMakerRequestObject sro = mapper.readValue(inputJson, SageMakerRequestObject.class);
        Assert.assertEquals(sro.getInput().size(), 3);
        Assert.assertEquals(sro.getInput().size(), 3);
        Assert.assertEquals(sro.getInput().get(0).getName(), "name_1");
        Assert.assertEquals(sro.getInput().get(1).getName(), "name_2");
        Assert.assertEquals(sro.getInput().get(2).getName(), "name_3");
        Assert.assertEquals(sro.getInput().get(0).getVal(), Lists.newArrayList(1, 2, 3));
        Assert.assertEquals(sro.getInput().get(1).getVal(), "C");
        Assert.assertEquals(sro.getInput().get(2).getVal(), Lists.newArrayList(38.0, 24.0));
        Assert.assertEquals(sro.getInput().get(0).getType(), "int");
        Assert.assertEquals(sro.getInput().get(1).getType(), "string");
        Assert.assertEquals(sro.getInput().get(2).getType(), "double");
        Assert.assertEquals(sro.getInput().get(0).getStructure(), "vector");
        Assert.assertEquals(sro.getInput().get(1).getStructure(), "basic");
        Assert.assertEquals(sro.getInput().get(2).getStructure(), "array");
        Assert.assertEquals(sro.getOutput().getName(), "features");
        Assert.assertEquals(sro.getOutput().getType(), "double");
        Assert.assertEquals(sro.getOutput().getStructure(), "vector");
    }
}
