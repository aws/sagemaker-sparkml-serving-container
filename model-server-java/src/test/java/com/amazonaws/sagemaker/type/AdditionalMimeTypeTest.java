package com.amazonaws.sagemaker.type;

import org.junit.Assert;
import org.junit.Test;

public class AdditionalMimeTypeTest {

    @Test
    public void testAdditionalMimeType() {
        Assert.assertEquals(AdditionalMimeType.TEXT_CSV.toString(), "text/csv");
        Assert.assertEquals(AdditionalMimeType.APPLICATION_JSONLINES.toString(), "application/jsonlines");
        Assert
            .assertEquals(AdditionalMimeType.APPLICATION_JSONLINES_TEXT.toString(), "application/jsonlines;data=text");
    }

}
