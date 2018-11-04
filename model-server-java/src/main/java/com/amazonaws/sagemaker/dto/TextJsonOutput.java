package com.amazonaws.sagemaker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class TextJsonOutput {

    private String source;

    @JsonCreator
    public TextJsonOutput(@JsonProperty("source") String source) {
        this.source = Preconditions.checkNotNull(source);
    }

    public String getSource() {
        return source;
    }
}
