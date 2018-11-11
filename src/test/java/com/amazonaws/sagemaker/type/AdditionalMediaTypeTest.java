package com.amazonaws.sagemaker.type;

import org.junit.Assert;
import org.junit.Test;

public class AdditionalMediaTypeTest {

    @Test
    public void testAdditionalMimeType() {
        Assert.assertEquals(AdditionalMediaType.TEXT_CSV_VALUE, "text/csv");
        Assert.assertEquals(AdditionalMediaType.APPLICATION_JSONLINES_VALUE, "application/jsonlines");
        Assert.assertEquals(AdditionalMediaType.APPLICATION_JSONLINES_TEXT_VALUE, "application/jsonlines;data=text");
    }

}
