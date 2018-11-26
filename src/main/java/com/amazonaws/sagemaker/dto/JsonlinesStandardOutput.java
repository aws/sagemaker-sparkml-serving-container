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
 * POJO class to represent the standard JSONlines output format for SageMaker built-in algorithms.
 */
public class JsonlinesStandardOutput {

    private List<Object> features;

    @JsonCreator
    public JsonlinesStandardOutput(@JsonProperty("features") final List<Object> features) {
        this.features = Preconditions.checkNotNull(features);
    }

    public List<Object> getFeatures() {
        return features;
    }

}
