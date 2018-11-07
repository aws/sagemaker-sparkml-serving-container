package com.amazonaws.sagemaker.dto;


import com.amazonaws.sagemaker.type.StructureType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Optional;

/**
 * POJO to represent single column of Spark data that MLeap will transform. Each column can be a basic value or a List
 * of basic values (for Spark Array or Vector).
 */
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
        this.name = Preconditions.checkNotNull(name);
        this.type = Preconditions.checkNotNull(type);
        this.structure = Optional.ofNullable(structure).orElse(StructureType.BASIC);
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

}
