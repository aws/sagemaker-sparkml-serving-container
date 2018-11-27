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
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class DataSchemaTest {

    private ObjectMapper mapper = new ObjectMapper();
    List<ColumnSchema> inputCols = Lists.newArrayList(new ColumnSchema("name_1", "type_1", "struct_1"),
        new ColumnSchema("name_2", "type_2", "struct_2"));
    ColumnSchema outputCol = new ColumnSchema("name_out_1", "type_out_1", "struct_out_1");

    @Test
    public void testDataSchemaObjectCreation() {
        DataSchema ds = new DataSchema(inputCols, outputCol);
        Assert.assertEquals(ds.getInput().get(0).getName(), "name_1");
        Assert.assertEquals(ds.getInput().get(0).getType(), "type_1");
        Assert.assertEquals(ds.getInput().get(0).getStruct(), "struct_1");
        Assert.assertEquals(ds.getInput().get(1).getName(), "name_2");
        Assert.assertEquals(ds.getInput().get(1).getType(), "type_2");
        Assert.assertEquals(ds.getInput().get(1).getStruct(), "struct_2");
        Assert.assertEquals(ds.getOutput().getName(), "name_out_1");
        Assert.assertEquals(ds.getOutput().getType(), "type_out_1");
        Assert.assertEquals(ds.getOutput().getStruct(), "struct_out_1");
    }

    @Test(expected = NullPointerException.class)
    public void testEmptyInputColumnsPassedToConstructor() {
        new DataSchema(null, outputCol);
    }

    @Test(expected = NullPointerException.class)
    public void testEmptyOutputColumnsPassedToConstructor() {
        new DataSchema(inputCols, null);
    }

    @Test
    public void testParseBasicInputJson() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("basic_input_schema.json"), "UTF-8");
        DataSchema schema = mapper.readValue(inputJson, DataSchema.class);
        Assert.assertEquals(schema.getInput().size(), 3);
        Assert.assertEquals(schema.getInput().get(0).getName(), "name_1");
        Assert.assertEquals(schema.getInput().get(1).getName(), "name_2");
        Assert.assertEquals(schema.getInput().get(2).getName(), "name_3");
        Assert.assertEquals(schema.getInput().get(0).getType(), "int");
        Assert.assertEquals(schema.getInput().get(1).getType(), "string");
        Assert.assertEquals(schema.getInput().get(2).getType(), "double");
        Assert.assertEquals(schema.getInput().get(0).getStruct(), "basic");
        Assert.assertEquals(schema.getInput().get(1).getStruct(), "basic");
        Assert.assertEquals(schema.getInput().get(2).getStruct(), "basic");
        Assert.assertEquals(schema.getOutput().getName(), "features");
        Assert.assertEquals(schema.getOutput().getType(), "double");
    }
}
