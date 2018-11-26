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
