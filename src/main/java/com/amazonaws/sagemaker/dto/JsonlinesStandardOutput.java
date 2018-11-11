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
