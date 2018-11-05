package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class BatchExecutionParameter {

    @JsonProperty("MaxConcurrentTransforms")
    private Integer maxConcurrentTransforms;

    @JsonProperty("BatchStrategy")
    private String batchStrategy;

    @JsonProperty("MaxPayloadInMB")
    private Integer maxPayloadInMB;

    @JsonCreator
    public BatchExecutionParameter(@JsonProperty("MaxConcurrentTransforms") Integer maxConcurrentTransforms,
        @JsonProperty("BatchStrategy") String batchStrategy, @JsonProperty("MaxPayloadInMB") Integer maxPayloadInMB) {
        this.maxConcurrentTransforms = Preconditions.checkNotNull(maxConcurrentTransforms);
        this.batchStrategy = Preconditions.checkNotNull(batchStrategy);
        this.maxPayloadInMB = Preconditions.checkNotNull(maxPayloadInMB);
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
