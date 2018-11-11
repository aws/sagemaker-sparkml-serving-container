package com.amazonaws.sagemaker.dto;


import com.amazonaws.sagemaker.type.DataStructureType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Optional;

/**
 * POJO to represent single column of Spark data that MLeap will transform. Each column can be a basic value or a List
 * of basic values (for Spark Array or Vector).
 */
public class ColumnSchema {

    private String name;
    private String type;
    private String struct;

    @JsonCreator
    public ColumnSchema(@JsonProperty("name") final String name, @JsonProperty("type") final String type,
        @JsonProperty("struct") final String struct) {
        this.name = Preconditions.checkNotNull(name);
        this.type = Preconditions.checkNotNull(type);
        this.struct = Optional.ofNullable(struct).orElse(DataStructureType.BASIC);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getStruct() {
        return struct;
    }

}
