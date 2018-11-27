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
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class JsonlinesTextOutputTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStandardJsonOutputObjectCreation() {
        JsonlinesTextOutput jsonlinesTextOutputTest = new JsonlinesTextOutput("this is spark ml server");
        Assert.assertEquals(jsonlinesTextOutputTest.getSource(), "this is spark ml server");
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputPassedToConstructor() {
        new JsonlinesTextOutput(null);
    }

    @Test
    public void testParseStandardJsonOutput() throws IOException {
        String inputJson = IOUtils.toString(this.getClass().getResourceAsStream("text_json_out.json"), "UTF-8");
        JsonlinesTextOutput sjo = mapper.readValue(inputJson, JsonlinesTextOutput.class);
        Assert.assertEquals(sjo.getSource(), "this is spark ml server");
    }

}
