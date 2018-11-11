package com.amazonaws.sagemaker.type;

/**
 * Each column in the input and output can be a single value (basic), Spark ArrayType(array) or Spark Vector type
 * (vector)
 */
public final class DataStructureType {

    public static final String BASIC = "basic";
    public static final String VECTOR = "vector";
    public static final String ARRAY = "array";

}
