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

import com.amazonaws.sagemaker.dto.DataSchema;
import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.type.BasicDataType;
import com.amazonaws.sagemaker.type.DataStructureType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ml.combust.mleap.core.types.ListType;
import ml.combust.mleap.core.types.ScalarType;
import ml.combust.mleap.core.types.TensorType;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class DataConversionHelperTest {

    private ObjectMapper mapper = new ObjectMapper();
    private DataConversionHelper dataConversionHelper = new DataConversionHelper(new LeapFrameBuilderSupport(),
        new LeapFrameBuilder());

    @Test
    public void testParseCsvToObjectList() throws IOException {
        String csvInput = "2,C,34.5";
        String inputJson = IOUtils
            .toString(this.getClass().getResourceAsStream("../dto/basic_input_schema.json"), "UTF-8");
        DataSchema schema = mapper.readValue(inputJson, DataSchema.class);
        List<Object> expectedElement = Lists.newArrayList(new Integer("2"), "C", new Double("34.5"));
        List<List<Object>> expectedOutput = Lists.newArrayList();
        expectedOutput.add(expectedElement);
        Assert.assertEquals(dataConversionHelper.convertCsvToObjectList(csvInput, schema), expectedOutput);
    }

    @Test
    public void testParseCsvQuotesToObjectList() throws IOException {
        String csvInput = "2,\"C\",34.5";
        String inputJson = IOUtils
            .toString(this.getClass().getResourceAsStream("../dto/basic_input_schema.json"), "UTF-8");
        DataSchema schema = mapper.readValue(inputJson, DataSchema.class);
        List<Object> expectedElement = Lists.newArrayList(new Integer("2"), "C", new Double("34.5"));
        List<List<Object>> expectedOutput = Lists.newArrayList();
        expectedOutput.add(expectedElement);
        Assert.assertEquals(dataConversionHelper.convertCsvToObjectList(csvInput, schema), expectedOutput);
    }

    @Test
    public void testCastingInputToLeapFrame() throws Exception {
        String inputJson = IOUtils
            .toString(this.getClass().getResourceAsStream("../dto/complete_input.json"), "UTF-8");
        SageMakerRequestObject sro = mapper.readValue(inputJson, SageMakerRequestObject.class);
        DefaultLeapFrame leapframeTest = dataConversionHelper.convertInputToLeapFrame(sro.getSchema(), Collections.singletonList(sro.getData()));
        Assert.assertNotNull(leapframeTest.schema());
        Assert.assertNotNull(leapframeTest.dataset());
    }

    @Test
    public void testCastingMLeapBasicTypeToJavaType() {
        ArrayRow testRow = new ArrayRow(Lists.newArrayList(new Integer("1")));
        Assert.assertEquals(new Integer("1"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.INTEGER));

        testRow = new ArrayRow(Lists.newArrayList(new Double("1.0")));
        Assert.assertEquals(new Double("1.0"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.FLOAT));

        testRow = new ArrayRow(Lists.newArrayList(new Long("1")));
        Assert.assertEquals(new Long("1"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.LONG));

        testRow = new ArrayRow(Lists.newArrayList(new Double("1.0")));
        Assert.assertEquals(new Double("1"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.DOUBLE));

        testRow = new ArrayRow(Lists.newArrayList(new Short("1")));
        Assert.assertEquals(new Short("1"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.SHORT));

        testRow = new ArrayRow(Lists.newArrayList(new Byte("1")));
        Assert.assertEquals(new Byte("1"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.BYTE));

        testRow = new ArrayRow(Lists.newArrayList(Boolean.valueOf("1")));
        Assert.assertEquals(Boolean.valueOf("1"),
            dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.BOOLEAN));

        testRow = new ArrayRow(Lists.newArrayList("1"));
        Assert.assertEquals("1", dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, BasicDataType.STRING));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingMleapBasicTypeToJavaTypeWrongInput() {
        ArrayRow testRow = new ArrayRow(Lists.newArrayList(new Integer("1")));
        Assert
            .assertEquals(new Integer("1"), dataConversionHelper.convertMLeapBasicTypeToJavaType(testRow, "intvalue"));
    }

    @Test
    public void testCastingInputToJavaTypeSingle() {
        Assert.assertEquals(new Integer("1"), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.INTEGER, DataStructureType.BASIC, new Integer("1")));

        Assert.assertEquals(new Float("1.0"), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.FLOAT, DataStructureType.BASIC, new Float("1.0")));

        Assert.assertEquals(new Double("1.0"), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.DOUBLE, DataStructureType.BASIC, new Double("1.0")));

        Assert.assertEquals(new Byte("1"),
            dataConversionHelper
                .convertInputDataToJavaType(BasicDataType.BYTE, DataStructureType.BASIC, new Byte("1")));

        Assert.assertEquals(new Long("1"),
            dataConversionHelper.convertInputDataToJavaType(BasicDataType.LONG, null, new Long("1")));

        Assert.assertEquals(new Short("1"),
            dataConversionHelper.convertInputDataToJavaType(BasicDataType.SHORT, null, new Short("1")));

        Assert.assertEquals("1", dataConversionHelper.convertInputDataToJavaType(BasicDataType.STRING, null, "1"));

        Assert.assertEquals(Boolean.valueOf("1"),
            dataConversionHelper.convertInputDataToJavaType(BasicDataType.BOOLEAN, null, Boolean.valueOf("1")));
    }

    @Test
    public void testCastingInputToJavaTypeList() {
        Assert.assertEquals(Lists.newArrayList(1, 2), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.INTEGER, DataStructureType.VECTOR,
                Lists.newArrayList(new Integer("1"), new Integer("2"))));

        Assert.assertEquals(Lists.newArrayList(1.0, 2.0), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.FLOAT, DataStructureType.VECTOR,
                Lists.newArrayList(new Double("1.0"), new Double("2.0"))));

        Assert.assertEquals(Lists.newArrayList(1.0, 2.0), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.DOUBLE, DataStructureType.VECTOR,
                Lists.newArrayList(new Double("1.0"), new Double("2.0"))));

        Assert.assertEquals(Lists.newArrayList(new Byte("1")), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.BYTE, DataStructureType.VECTOR,
                Lists.newArrayList(new Byte("1"))));

        Assert.assertEquals(Lists.newArrayList(1L, 2L), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.LONG, DataStructureType.ARRAY,
                Lists.newArrayList(new Long("1"), new Long("2"))));

        Assert.assertEquals(Lists.newArrayList(new Short("1")), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.SHORT, DataStructureType.ARRAY,
                Lists.newArrayList(new Short("1"))));

        Assert.assertEquals(Lists.newArrayList("1"), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.STRING, DataStructureType.ARRAY, Lists.newArrayList("1")));

        Assert.assertEquals(Lists.newArrayList(Boolean.valueOf("1")), dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.BOOLEAN, DataStructureType.ARRAY,
                Lists.newArrayList(Boolean.valueOf("1"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToJavaTypeNonList() {
        dataConversionHelper
            .convertInputDataToJavaType(BasicDataType.INTEGER, DataStructureType.VECTOR, new Integer("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToJavaTypeWrongType() {
        dataConversionHelper.convertInputDataToJavaType("intvalue", DataStructureType.BASIC, new Integer("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToJavaTypeListWrongType() {
        dataConversionHelper.convertInputDataToJavaType("intvalue", DataStructureType.VECTOR, Lists.newArrayList(1, 2));
    }

    @Test
    public void testCastingInputToMLeapType() {
        Assert.assertTrue(dataConversionHelper
            .convertInputToMLeapInputType(BasicDataType.INTEGER, DataStructureType.BASIC) instanceof ScalarType);

        Assert.assertTrue(
            dataConversionHelper.convertInputToMLeapInputType(BasicDataType.FLOAT, null) instanceof ScalarType);

        Assert.assertTrue(dataConversionHelper
            .convertInputToMLeapInputType(BasicDataType.DOUBLE, DataStructureType.VECTOR) instanceof TensorType);

        Assert.assertTrue(dataConversionHelper
            .convertInputToMLeapInputType(BasicDataType.LONG, DataStructureType.ARRAY) instanceof ListType);

        Assert.assertTrue(dataConversionHelper
            .convertInputToMLeapInputType(BasicDataType.STRING, DataStructureType.BASIC) instanceof ScalarType);

        Assert.assertTrue(
            dataConversionHelper.convertInputToMLeapInputType(BasicDataType.SHORT, null) instanceof ScalarType);

        Assert.assertTrue(dataConversionHelper
            .convertInputToMLeapInputType(BasicDataType.BYTE, DataStructureType.ARRAY) instanceof ListType);

        Assert.assertTrue(dataConversionHelper
            .convertInputToMLeapInputType(BasicDataType.BOOLEAN, DataStructureType.VECTOR) instanceof TensorType);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToMLeapTypeWrongType() {
        dataConversionHelper.convertInputToMLeapInputType("intvalue", DataStructureType.VECTOR);
    }


}
