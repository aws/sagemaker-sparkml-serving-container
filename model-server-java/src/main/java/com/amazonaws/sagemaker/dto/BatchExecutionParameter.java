package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchExecutionParameter {

    @JsonProperty("MaxConcurrentTransforms")
    private Integer maxConcurrentTransforms;

    @JsonProperty("BatchStrategy")
    private String batchStrategy;

    @JsonProperty("MaxPayloadInMB")
    private Integer maxPayloadInMB;

    @JsonCreator
    public BatchExecutionParameter(final Integer maxConcurrentTransforms, final String batchStrategy,
        final Integer maxPayloadInMB) {
        this.maxConcurrentTransforms = maxConcurrentTransforms;
        this.batchStrategy = batchStrategy;
        this.maxPayloadInMB = maxPayloadInMB;
    }

    public Integer getMaxConcurrentTransforms() {
        return maxConcurrentTransforms;
    }

    public String getBatchStrategy() {
        return batchStrategy;
    }

    public Integer getMaxPayloadInMB() {
        return maxPayloadInMB;
    }

}
