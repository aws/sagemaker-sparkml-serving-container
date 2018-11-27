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

/**
 * POJO class to represent the standard JSONlines output format for SageMaker NLP algorithms (BlazingText, Seq2Seq)
 */
public class JsonlinesTextOutput {

    private String source;

    @JsonCreator
    public JsonlinesTextOutput(@JsonProperty("source") final String source) {
        this.source = Preconditions.checkNotNull(source);
    }

    public String getSource() {
        return source;
    }
}
