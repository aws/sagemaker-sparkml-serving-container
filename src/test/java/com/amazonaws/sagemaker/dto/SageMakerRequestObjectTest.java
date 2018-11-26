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
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class SageMakerRequestObjectTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSageMakerRequestObjectCreation() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("basic_input_schema.json"), "UTF-8");
        DataSchema schema = mapper.readValue(inputJson, DataSchema.class);
        SageMakerRequestObject sro = new SageMakerRequestObject(schema, Lists.newArrayList(1, "C", 38.0));
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
        Assert.assertEquals(sro.getData(), Lists.newArrayList(1, "C", 38.0));
        Assert.assertEquals(sro.getSchema().getOutput().getName(), "features");
        Assert.assertEquals(sro.getSchema().getOutput().getType(), "double");
    }

    @Test(expected = NullPointerException.class)
    public void testNullDataPassedToConstructor() {
        new SageMakerRequestObject(null, null);
    }

    @Test
    public void testParseBasicInputJson() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("basic_input.json"), "UTF-8");
        SageMakerRequestObject sro = mapper.readValue(inputJson, SageMakerRequestObject.class);
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
        Assert.assertEquals(sro.getData(), Lists.newArrayList(1, "C", 38.0));
        Assert.assertEquals(sro.getSchema().getOutput().getName(), "features");
        Assert.assertEquals(sro.getSchema().getOutput().getType(), "double");
    }

    @Test
    public void testParseCompleteInputJson() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("complete_input.json"), "UTF-8");
        SageMakerRequestObject sro = mapper.readValue(inputJson, SageMakerRequestObject.class);
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
        Assert.assertEquals(sro.getData(),
            Lists.newArrayList(Lists.newArrayList(1, 2, 3), "C", Lists.newArrayList(38.0, 24.0)));
        Assert.assertEquals(sro.getSchema().getOutput().getName(), "features");
        Assert.assertEquals(sro.getSchema().getOutput().getType(), "double");
        Assert.assertEquals(sro.getSchema().getOutput().getStruct(), "vector");

    }
}
