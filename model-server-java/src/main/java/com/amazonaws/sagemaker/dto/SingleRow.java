package com.amazonaws.sagemaker.dto;


public class SingleRow {
    private String columnName;
    // input should be one of strings from {@See BaseDataTypes}
    private String dataType;
    private Boolean isList;
    // input can be one of the {@See BaseDataTypes} or a list of {@See BaseDataTypes}
    private Object input;

    public SingleRow(String columnName, String dataType, Boolean isList, Object input) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.isList = isList;
        this.input = input;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getList() {
        return isList;
    }

    public void setList(Boolean list) {
        isList = list;
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        this.input = input;
    }
}
