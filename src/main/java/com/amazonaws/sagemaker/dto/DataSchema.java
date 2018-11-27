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
 * Input schema for the request paylod. This can either be passed via an environment variable or part of a request.
 * If the schema is present in both the environment variable and the request, the one in request will take precedence.
 */
public class DataSchema {

    private List<ColumnSchema> input;
    private ColumnSchema output;

    @JsonCreator
    public DataSchema(@JsonProperty("input") final List<ColumnSchema> input,
        @JsonProperty("output") final ColumnSchema output) {
        this.input = Preconditions.checkNotNull(input);
        this.output = Preconditions.checkNotNull(output);
    }

    public List<ColumnSchema> getInput() {
        return input;
    }

    public ColumnSchema getOutput() {
        return output;
    }
}
