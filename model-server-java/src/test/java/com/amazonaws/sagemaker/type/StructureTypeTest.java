package com.amazonaws.sagemaker.type;

import org.junit.Assert;
import org.junit.Test;

public class StructureTypeTest {

    @Test
    public void testStructureType() {
        Assert.assertEquals(StructureType.BASIC, "basic");
        Assert.assertEquals(StructureType.VECTOR, "vector");
        Assert.assertEquals(StructureType.ARRAY, "array");
    }

}
