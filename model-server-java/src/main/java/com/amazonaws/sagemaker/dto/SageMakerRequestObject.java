package com.amazonaws.sagemaker.dto;

public class SageMakerRequestObject {
    private String outputColumn;
    private SingleRow singleRow;
    private Boolean isOutputList;

    public SageMakerRequestObject(String outputColumn, SingleRow singleRow, Boolean isOutputList) {
        this.outputColumn = outputColumn;
        this.singleRow = singleRow;
        this.isOutputList = isOutputList;
    }

    public String getOutputColumn() {
        return outputColumn;
    }

    public void setOutputColumn(String outputColumn) {
        this.outputColumn = outputColumn;
    }

    public SingleRow getSingleRow() {
        return singleRow;
    }

    public void setSingleRow(SingleRow singleRow) {
        this.singleRow = singleRow;
    }

    public Boolean getOutputList() {
        return isOutputList;
    }

    public void setOutputList(Boolean outputList) {
        isOutputList = outputList;
    }
}
