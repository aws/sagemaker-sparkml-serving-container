package com.amazonaws.sagemaker.dto;

public class SageMakerResponseObject {
    // response should be one of the supported primitive types and String or a list of these
    private Object response;

    public SageMakerResponseObject(final Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
