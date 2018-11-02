package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StandardJsonOutput {

    private List<Object> features;

    @JsonCreator
    public StandardJsonOutput(@JsonProperty("features") List<Object> features) {
        this.features = features;
    }

    public List<Object> getFeatures() {
        return features;
    }
}
