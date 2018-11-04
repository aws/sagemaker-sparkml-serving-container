package com.amazonaws.sagemaker.type;

import org.junit.Assert;
import org.junit.Test;

public class BasicDataTypeTest {

    @Test
    public void testBasicDataType() {
        Assert.assertEquals(BasicDataType.BOOLEAN, "boolean");
        Assert.assertEquals(BasicDataType.INTEGER, "int");
        Assert.assertEquals(BasicDataType.FLOAT, "float");
        Assert.assertEquals(BasicDataType.LONG, "long");
        Assert.assertEquals(BasicDataType.DOUBLE, "double");
        Assert.assertEquals(BasicDataType.SHORT, "short");
        Assert.assertEquals(BasicDataType.BYTE, "byte");
        Assert.assertEquals(BasicDataType.STRING, "string");
    }
}
