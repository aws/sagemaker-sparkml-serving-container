package com.amazonaws.sagemaker.converter;

import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.type.BasicDataType;
import com.amazonaws.sagemaker.type.StructureType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import ml.combust.mleap.core.types.ListType;
import ml.combust.mleap.core.types.ScalarType;
import ml.combust.mleap.core.types.TensorType;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataTypeConverterTest {

    private ObjectMapper mapper;
    private DataTypeConverter converter;

    @Before
    public void setup() {

        mapper = new ObjectMapper();
        converter = new DataTypeConverter(new LeapFrameBuilderSupport(), new LeapFrameBuilder());
    }

    @Test
    public void testCastingInputToLeapFrame() throws Exception {
        String inputJson = IOUtils
            .toString(this.getClass().getResourceAsStream("../dto/sagemaker_input_2.json"), "UTF-8");
        SageMakerRequestObject sro = mapper.readValue(inputJson, SageMakerRequestObject.class);
        DefaultLeapFrame leapframeTest = converter.castInputToLeapFrame(sro);
        Assert.assertNotNull(leapframeTest.schema());
        Assert.assertNotNull(leapframeTest.dataset());
    }

    @Test
    public void testCastingMLeapBasicTypeToJavaType() {
        ArrayRow testRow = new ArrayRow(Lists.newArrayList(new Integer("1")));
        Assert.assertEquals(new Integer("1"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.INTEGER));

        testRow = new ArrayRow(Lists.newArrayList(new Float("1.0")));
        Assert.assertEquals(new Float("1.0"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.FLOAT));

        testRow = new ArrayRow(Lists.newArrayList(new Long("1")));
        Assert.assertEquals(new Long("1"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.LONG));

        testRow = new ArrayRow(Lists.newArrayList(new Double("1.0")));
        Assert.assertEquals(new Double("1"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.DOUBLE));

        testRow = new ArrayRow(Lists.newArrayList(new Short("1")));
        Assert.assertEquals(new Short("1"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.SHORT));

        testRow = new ArrayRow(Lists.newArrayList(new Byte("1")));
        Assert.assertEquals(new Byte("1"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.BYTE));

        testRow = new ArrayRow(Lists.newArrayList(Boolean.valueOf("1")));
        Assert
            .assertEquals(Boolean.valueOf("1"), converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.BOOLEAN));

        testRow = new ArrayRow(Lists.newArrayList("1"));
        Assert.assertEquals("1", converter.castMLeapBasicTypeToJavaType(testRow, BasicDataType.STRING));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingMleapBasicTypeToJavaTypeWrongInput() {
        ArrayRow testRow = new ArrayRow(Lists.newArrayList(new Integer("1")));
        Assert.assertEquals(new Integer("1"), converter.castMLeapBasicTypeToJavaType(testRow, "intvalue"));
    }

    @Test
    public void testCastingInputToJavaTypeSingle() {
        Assert.assertEquals(new Integer("1"),
            converter.castInputToJavaType(BasicDataType.INTEGER, StructureType.BASIC, new Integer("1")));

        Assert.assertEquals(new Float("1.0"),
            converter.castInputToJavaType(BasicDataType.FLOAT, StructureType.BASIC, new Float("1.0")));

        Assert.assertEquals(new Double("1.0"),
            converter.castInputToJavaType(BasicDataType.DOUBLE, StructureType.BASIC, new Double("1.0")));

        Assert.assertEquals(new Byte("1"),
            converter.castInputToJavaType(BasicDataType.BYTE, StructureType.BASIC, new Byte("1")));

        Assert.assertEquals(new Long("1"), converter.castInputToJavaType(BasicDataType.LONG, null, new Long("1")));

        Assert.assertEquals(new Short("1"), converter.castInputToJavaType(BasicDataType.SHORT, null, new Short("1")));

        Assert.assertEquals("1", converter.castInputToJavaType(BasicDataType.STRING, null, "1"));

        Assert.assertEquals(Boolean.valueOf("1"),
            converter.castInputToJavaType(BasicDataType.BOOLEAN, null, Boolean.valueOf("1")));
    }

    @Test
    public void testCastingInputToJavaTypeList() {
        Assert.assertEquals(Lists.newArrayList(1, 2), converter
            .castInputToJavaType(BasicDataType.INTEGER, StructureType.VECTOR,
                Lists.newArrayList(new Integer("1"), new Integer("2"))));

        Assert.assertEquals(Lists.newArrayList(1.0, 2.0), converter
            .castInputToJavaType(BasicDataType.FLOAT, StructureType.VECTOR,
                Lists.newArrayList(new Double("1.0"), new Double("2.0"))));

        Assert.assertEquals(Lists.newArrayList(1.0, 2.0), converter
            .castInputToJavaType(BasicDataType.DOUBLE, StructureType.VECTOR,
                Lists.newArrayList(new Double("1.0"), new Double("2.0"))));

        Assert.assertEquals(Lists.newArrayList(new Byte("1")),
            converter.castInputToJavaType(BasicDataType.BYTE, StructureType.VECTOR, Lists.newArrayList(new Byte("1"))));

        Assert.assertEquals(Lists.newArrayList(1L, 2L), converter
            .castInputToJavaType(BasicDataType.LONG, StructureType.ARRAY,
                Lists.newArrayList(new Long("1"), new Long("2"))));

        Assert.assertEquals(Lists.newArrayList(new Short("1")), converter
            .castInputToJavaType(BasicDataType.SHORT, StructureType.ARRAY, Lists.newArrayList(new Short("1"))));

        Assert.assertEquals(Lists.newArrayList("1"),
            converter.castInputToJavaType(BasicDataType.STRING, StructureType.ARRAY, Lists.newArrayList("1")));

        Assert.assertEquals(Lists.newArrayList(Boolean.valueOf("1")), converter
            .castInputToJavaType(BasicDataType.BOOLEAN, StructureType.ARRAY, Lists.newArrayList(Boolean.valueOf("1"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToJavaTypeNonList() {
        converter.castInputToJavaType(BasicDataType.INTEGER, StructureType.VECTOR, new Integer("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToJavaTypeWrongType() {
        converter.castInputToJavaType("intvalue", StructureType.BASIC, new Integer("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToJavaTypeListWrongType() {
        converter.castInputToJavaType("intvalue", StructureType.VECTOR, Lists.newArrayList(1, 2));
    }

    @Test
    public void testCastingInputToMLeapType() {
        Assert.assertTrue(
            converter.castInputToMLeapInputType(BasicDataType.INTEGER, StructureType.BASIC) instanceof ScalarType);

        Assert.assertTrue(converter.castInputToMLeapInputType(BasicDataType.FLOAT, null) instanceof ScalarType);

        Assert.assertTrue(
            converter.castInputToMLeapInputType(BasicDataType.DOUBLE, StructureType.VECTOR) instanceof TensorType);

        Assert.assertTrue(
            converter.castInputToMLeapInputType(BasicDataType.LONG, StructureType.ARRAY) instanceof ListType);

        Assert.assertTrue(
            converter.castInputToMLeapInputType(BasicDataType.STRING, StructureType.BASIC) instanceof ScalarType);

        Assert.assertTrue(converter.castInputToMLeapInputType(BasicDataType.SHORT, null) instanceof ScalarType);

        Assert.assertTrue(
            converter.castInputToMLeapInputType(BasicDataType.BYTE, StructureType.ARRAY) instanceof ListType);

        Assert.assertTrue(
            converter.castInputToMLeapInputType(BasicDataType.BOOLEAN, StructureType.VECTOR) instanceof TensorType);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCastingInputToMLeapTypeWrongType() {
        converter.castInputToMLeapInputType("intvalue", StructureType.VECTOR);
    }


}
