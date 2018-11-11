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

    private DataSchema schema;
    private List<Object> data;

    @JsonCreator
    public SageMakerRequestObject(@JsonProperty("schema") final DataSchema schema,
        @JsonProperty("data") final List<Object> data) {
        // schema can be retrieved from environment variable as well, hence it is not enforced to be null
        this.schema = schema;
        this.data = Preconditions.checkNotNull(data);
    }

    public DataSchema getSchema() {
        return schema;
    }

    public List<Object> getData() {
        return data;
    }
}
