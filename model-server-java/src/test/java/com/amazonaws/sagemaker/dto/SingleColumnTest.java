package com.amazonaws.sagemaker.dto;

import com.amazonaws.sagemaker.type.StructureType;
import org.junit.Assert;
import org.junit.Test;

public class SingleColumnTest {

    @Test
    public void testSingleColumnObjectCreation() {
        SingleColumn singleColumnTest = new SingleColumn("test_name", "test_type", "test_struct", "test_val");
        Assert.assertEquals(singleColumnTest.getName(), "test_name");
        Assert.assertEquals(singleColumnTest.getType(), "test_type");
        Assert.assertEquals(singleColumnTest.getStructure(), "test_struct");
        Assert.assertEquals(singleColumnTest.getVal(), "test_val");
    }

    @Test(expected = NullPointerException.class)
    public void testNullNamePassedToConstructor() {
        new SingleColumn(null, "test_type", "test_struct", "test_val");
    }

    @Test(expected = NullPointerException.class)
    public void testNullTypePassedToConstructor() {
        new SingleColumn("test_name", null, "test_struct", "test_val");
    }

    @Test
    public void testNullStructPassedToConstructor() {
        SingleColumn singleColumnTest = new SingleColumn("test_name", "test_type", null, "test_val");
        Assert.assertEquals(singleColumnTest.getStructure(), StructureType.BASIC);
    }

}
