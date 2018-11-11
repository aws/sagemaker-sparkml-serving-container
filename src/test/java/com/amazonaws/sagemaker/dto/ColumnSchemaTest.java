package com.amazonaws.sagemaker.dto;

import com.amazonaws.sagemaker.type.DataStructureType;
import org.junit.Assert;
import org.junit.Test;

public class ColumnSchemaTest {

    @Test
    public void testSingleColumnObjectCreation() {
        ColumnSchema columnSchemaTest = new ColumnSchema("test_name", "test_type", "test_struct");
        Assert.assertEquals(columnSchemaTest.getName(), "test_name");
        Assert.assertEquals(columnSchemaTest.getType(), "test_type");
        Assert.assertEquals(columnSchemaTest.getStruct(), "test_struct");
    }

    @Test(expected = NullPointerException.class)
    public void testNullNamePassedToConstructor() {
        new ColumnSchema(null, "test_type", "test_struct");
    }

    @Test(expected = NullPointerException.class)
    public void testNullTypePassedToConstructor() {
        new ColumnSchema("test_name", null, "test_struct");
    }

    @Test
    public void testNullStructPassedToConstructor() {
        ColumnSchema columnSchemaTest = new ColumnSchema("test_name", "test_type", null);
        Assert.assertEquals(columnSchemaTest.getStruct(), DataStructureType.BASIC);
    }

}
