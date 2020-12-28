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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Request object POJO to which data field of input request in JSONLINES format will be mapped to by Spring (using Jackson).
 * For sample input, please see test/resources/com/amazonaws/sagemaker/dto
 */
public class SageMakerRequestListObject {

    private DataSchema schema;
    private List<List<Object>> data;

    @JsonCreator
    public SageMakerRequestListObject(@JsonProperty("schema") final DataSchema schema,
                                      @JsonProperty("data") final List<List<Object>> data) {
        // schema can be retrieved from environment variable as well, hence it is not enforced to be null
        this.schema = schema;
        this.data = Preconditions.checkNotNull(data);
    }

    public DataSchema getSchema() {
        return schema;
    }

    public List<List<Object>> getData() {
        return data;
    }
}
