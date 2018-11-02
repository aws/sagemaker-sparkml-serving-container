package com.amazonaws.sagemaker.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleColumn {

    private String name;
    // input should be one of strings from {@See BaseDataTypes}
    private String type;
    private String structure;
    // input can be one of the {@See BaseDataTypes} or a list of {@See BaseDataTypes}
    private Object val;

    @JsonCreator
    public SingleColumn(@JsonProperty("name") String name, @JsonProperty("type") String type,
        @JsonProperty("struct") String structure, @JsonProperty("val") Object val) {
        this.name = name;
        this.type = type;
        this.structure = structure;
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getStructure() {
        return structure;
    }

    public Object getVal() {
        return val;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SingleColumn.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("type='" + type + "'")
            .add("structure='" + structure + "'")
            .add("val=" + val)
            .toString();
    }
}
