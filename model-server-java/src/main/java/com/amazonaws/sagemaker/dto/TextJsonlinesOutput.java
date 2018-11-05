package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * POJO class to represent the standard JSONlines output format for SageMaker NLP algorithms (BlazingText, Seq2Seq)
 */
public class TextJsonlinesOutput {

    private String source;

    @JsonCreator
    public TextJsonlinesOutput(@JsonProperty("source") String source) {
        this.source = Preconditions.checkNotNull(source);
    }

    public String getSource() {
        return source;
    }
}
