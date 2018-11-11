package com.amazonaws.sagemaker.type;

import org.junit.Assert;
import org.junit.Test;

public class DataStructureTypeTest {

    @Test
    public void testStructureType() {
        Assert.assertEquals(DataStructureType.BASIC, "basic");
        Assert.assertEquals(DataStructureType.VECTOR, "vector");
        Assert.assertEquals(DataStructureType.ARRAY, "array");
    }

}
