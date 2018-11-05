package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.List;

public class StandardJsonOutput {

    private List<Object> features;

    @JsonCreator
    public StandardJsonOutput(@JsonProperty("features") List<Object> features) {
        this.features = Preconditions.checkNotNull(features);
    }

    public List<Object> getFeatures() {
        return features;
    }

}
