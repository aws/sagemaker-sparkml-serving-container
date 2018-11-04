package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.List;

public class SageMakerRequestObject {

    private List<SingleColumn> input;
    private SingleColumn output;

    @JsonCreator
    public SageMakerRequestObject(@JsonProperty("input") List<SingleColumn> input,
        @JsonProperty("output") SingleColumn output) {
        this.input = Preconditions.checkNotNull(input);
        this.output = Preconditions.checkNotNull(output);
    }

    public List<SingleColumn> getInput() {
        return input;
    }

    public SingleColumn getOutput() {
        return output;
    }

}
