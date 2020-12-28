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

package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SageMakerRequestListObjectTest {

    private ObjectMapper mapper = new ObjectMapper();
    private List<List<Object>> listOfListInputForBasicInput;
    private List<List<Object>> listOfListInputForMultipleInput;

    @Before
    public void setup(){
        listOfListInputForBasicInput = new ArrayList<>();
        listOfListInputForBasicInput.add(Lists.newArrayList(1, "C", 38.0));
        listOfListInputForBasicInput.add(Lists.newArrayList(2, "D", 39.0));

        listOfListInputForMultipleInput = new ArrayList<>();
        listOfListInputForMultipleInput.add(Lists.newArrayList(Lists.newArrayList(1, 2, 3), "C",
                Lists.newArrayList(38.0, 24.0)));
        listOfListInputForMultipleInput.add(Lists.newArrayList(Lists.newArrayList(4, 5, 6), "D",
                Lists.newArrayList(39.0, 25.0)));
    }

    @Test
    public void testSageMakerRequestListObjectCreation() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("basic_input_schema.json"), "UTF-8");
        DataSchema schema = mapper.readValue(inputJson, DataSchema.class);
        SageMakerRequestListObject sro = new SageMakerRequestListObject(schema, listOfListInputForBasicInput);
        Assert.assertEquals(sro.getSchema().getInput().size(), 3);
        Assert.assertEquals(sro.getSchema().getInput().get(0).getName(), "name_1");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getName(), "name_2");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getName(), "name_3");
        Assert.assertEquals(sro.getSchema().getInput().get(0).getType(), "int");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getType(), "string");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getType(), "double");
        Assert.assertEquals(sro.getSchema().getInput().get(0).getStruct(), "basic");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getStruct(), "basic");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getStruct(), "basic");
        Assert.assertEquals(sro.getData(),listOfListInputForBasicInput);
        Assert.assertEquals(sro.getSchema().getOutput().getName(), "features");
        Assert.assertEquals(sro.getSchema().getOutput().getType(), "double");
    }

    @Test(expected = NullPointerException.class)
    public void testNullDataPassedToConstructor() {
        new SageMakerRequestListObject(null, null);
    }

    @Test
    public void testParseBasicMultipleLinesInputJson() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("basic_multipleLines_input.json"), "UTF-8");
        SageMakerRequestListObject sro = mapper.readValue(inputJson, SageMakerRequestListObject.class);
        Assert.assertEquals(sro.getSchema().getInput().size(), 3);
        Assert.assertEquals(sro.getSchema().getInput().get(0).getName(), "name_1");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getName(), "name_2");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getName(), "name_3");
        Assert.assertEquals(sro.getSchema().getInput().get(0).getType(), "int");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getType(), "string");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getType(), "double");
        Assert.assertEquals(sro.getSchema().getInput().get(0).getStruct(), "basic");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getStruct(), "basic");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getStruct(), "basic");
        Assert.assertEquals(sro.getData(), listOfListInputForBasicInput);
        Assert.assertEquals(sro.getSchema().getOutput().getName(), "features");
        Assert.assertEquals(sro.getSchema().getOutput().getType(), "double");
    }

    @Test
    public void testParseCompleteMultipleLinesInputJson() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("complete_multipleLines_input.json"), "UTF-8");
        SageMakerRequestListObject sro = mapper.readValue(inputJson, SageMakerRequestListObject.class);
        Assert.assertEquals(sro.getSchema().getInput().size(), 3);
        Assert.assertEquals(sro.getSchema().getInput().size(), 3);
        Assert.assertEquals(sro.getSchema().getInput().get(0).getName(), "name_1");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getName(), "name_2");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getName(), "name_3");
        Assert.assertEquals(sro.getSchema().getInput().get(0).getType(), "int");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getType(), "string");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getType(), "double");
        Assert.assertEquals(sro.getSchema().getInput().get(0).getStruct(), "vector");
        Assert.assertEquals(sro.getSchema().getInput().get(1).getStruct(), "basic");
        Assert.assertEquals(sro.getSchema().getInput().get(2).getStruct(), "array");
        Assert.assertEquals(sro.getData(), listOfListInputForMultipleInput);
        Assert.assertEquals(sro.getSchema().getOutput().getName(), "features");
        Assert.assertEquals(sro.getSchema().getOutput().getType(), "double");
        Assert.assertEquals(sro.getSchema().getOutput().getStruct(), "vector");
    }
}
