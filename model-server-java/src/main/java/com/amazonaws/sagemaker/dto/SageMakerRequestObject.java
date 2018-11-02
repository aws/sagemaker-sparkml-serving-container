package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.StringJoiner;

public class SageMakerRequestObject {

    private List<SingleColumn> input;
    private SingleColumn output;

    @JsonCreator
    public SageMakerRequestObject(@JsonProperty("input") List<SingleColumn> input,
        @JsonProperty("output") SingleColumn output) {
        this.input = input;
        this.output = output;
    }

    public List<SingleColumn> getInput() {
        return input;
    }

    public SingleColumn getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SageMakerRequestObject.class.getSimpleName() + "[", "]").add("input=" + input)
            .add("output=" + output).toString();
    }
}
