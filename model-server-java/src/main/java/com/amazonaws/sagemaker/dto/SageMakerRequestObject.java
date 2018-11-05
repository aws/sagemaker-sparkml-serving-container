package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.List;

/**
 * Request object POJO to which input request in JSON format will be mapped to by Spring (using Jackson). For sample
 * input, please see test/resources/com/amazonaws/sagemaker/dto
 */
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
