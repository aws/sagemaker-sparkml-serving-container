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
