/*
 *  Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 *  A copy of the License is located at
 *
 *      http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */

package com.amazonaws.sagemaker.helper;

import com.amazonaws.sagemaker.dto.DataSchema;
import com.amazonaws.sagemaker.dto.ColumnSchema;
import com.amazonaws.sagemaker.type.BasicDataType;
import com.amazonaws.sagemaker.type.DataStructureType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import ml.combust.mleap.core.types.BasicType;
import ml.combust.mleap.core.types.DataType;
import ml.combust.mleap.core.types.ListType;
import ml.combust.mleap.core.types.ScalarType;
import ml.combust.mleap.core.types.StructField;
import ml.combust.mleap.core.types.StructType;
import ml.combust.mleap.core.types.TensorType;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converter class to convert data between input to MLeap expected types and convert back MLeap helper to Java types
 * for output.
 */
@Component
public class DataConversionHelper {

    private final LeapFrameBuilderSupport support;
    private final LeapFrameBuilder leapFrameBuilder;

    @Autowired
    public DataConversionHelper(final LeapFrameBuilderSupport support, final LeapFrameBuilder leapFrameBuilder) {
        this.support = Preconditions.checkNotNull(support);
        this.leapFrameBuilder = Preconditions.checkNotNull(leapFrameBuilder);
    }


    /**
     * Parses the input payload in CSV format to a list of Objects
     * @param csvInput, the input received from the request in CSV format
     * @param schema, the data schema retrieved from environment variable
     * @return List of Objects, where each Object correspond to one feature of the input data
     * @throws IOException, if there is an exception thrown in the try-with-resources block
     */
    public List<List<Object>> convertCsvToObjectList(final String csvInput, final DataSchema schema) throws IOException {
        try (final StringReader sr = new StringReader(csvInput)) {
            final CSVParser parser = CSVFormat.DEFAULT.parse(sr);
            // We don not supporting multiple CSV lines as input currently
            final List<CSVRecord> records = parser.getRecords();
            final int inputLength = schema.getInput().size();

            final List<List<Object>> returnList = Lists.newArrayList();

            for(CSVRecord record : records) {
                final List<Object> valueList = Lists.newArrayList();
                for (int idx = 0; idx < inputLength; ++idx) {
                    ColumnSchema sc = schema.getInput().get(idx);
                    // For CSV input, each value is treated as an individual feature by default
                    valueList.add(this.convertInputDataToJavaType(sc.getType(), DataStructureType.BASIC, record.get(idx)));
                }
                returnList.add(valueList);
            }

            return returnList;
        }
    }


    /**
     * Convert input object to DefaultLeapFrame
     *
     * @param schema, the input schema received from request or environment variable
     * @param datas , the input datas received from request as a list of objects
     * @return the DefaultLeapFrame object which MLeap transformer expects
     */
    public DefaultLeapFrame convertInputToLeapFrame(final DataSchema schema, final List<List<Object>> datas) {

        final int inputLength = schema.getInput().size();
        final List<StructField> structFieldList = Lists.newArrayList();

        for (int idx = 0; idx < inputLength; ++idx) {
            ColumnSchema sc = schema.getInput().get(idx);
            structFieldList
                    .add(new StructField(sc.getName(), this.convertInputToMLeapInputType(sc.getType(), sc.getStruct())));
        }
        final StructType mleapSchema = leapFrameBuilder.createSchema(structFieldList);

        final List<Row> rows = Lists.newArrayList();

        for(Object data : datas)
        {
            final Row currentRow = getRow(schema, (List) data, inputLength);

            rows.add(currentRow);
        }

        return leapFrameBuilder.createFrame(mleapSchema, rows);
    }

    private Row getRow(DataSchema schema, List<Object> data, int inputLength) {
        final List<Object> valueList = Lists.newArrayList();

        for (int idx = 0; idx < inputLength; ++idx) {
            ColumnSchema sc = schema.getInput().get(idx);
            valueList.add(this.convertInputDataToJavaType(sc.getType(), sc.getStruct(), data.get(idx)));
        }

        return support.createRowFromIterable(valueList);
    }

    /**
     * Convert basic types in the MLeap helper to Java types for output.
     *
     * @param predictionRow, the ArrayRow from MLeapResponse
     * @param type, the basic type to which the helper should be casted, provided by user via input
     * @return the proper Java type
     */
    public Object convertMLeapBasicTypeToJavaType(final Row predictionRow, final String type) {
        switch (type) {
            case BasicDataType.INTEGER:
                return predictionRow.getInt(0);
            case BasicDataType.LONG:
                return predictionRow.getLong(0);
            case BasicDataType.FLOAT:
            case BasicDataType.DOUBLE:
                return predictionRow.getDouble(0);
            case BasicDataType.BOOLEAN:
                return predictionRow.getBool(0);
            case BasicDataType.BYTE:
                return predictionRow.getByte(0);
            case BasicDataType.SHORT:
                return predictionRow.getShort(0);
            case BasicDataType.STRING:
                return predictionRow.getString(0);
            default:
                throw new IllegalArgumentException("Given type is not supported");
        }
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    protected Object convertInputDataToJavaType(final String type, final String structure, final Object value) {
        if (StringUtils.isBlank(structure) || StringUtils.equals(structure, DataStructureType.BASIC)) {
            switch (type) {
                case BasicDataType.INTEGER:
                    return new Integer(value.toString());
                case BasicDataType.LONG:
                    return new Long(value.toString());
                case BasicDataType.FLOAT:
                    return new Float(value.toString());
                case BasicDataType.DOUBLE:
                    return new Double(value.toString());
                case BasicDataType.BOOLEAN:
                    return Boolean.valueOf(value.toString());
                case BasicDataType.BYTE:
                    return new Byte(value.toString());
                case BasicDataType.SHORT:
                    return new Short(value.toString());
                case BasicDataType.STRING:
                    return value.toString();
                default:
                    throw new IllegalArgumentException("Given type is not supported");
            }
        } else {
            List<Object> listOfObjects;
            try {
                listOfObjects = (List<Object>) value;
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException("Input val is not a list but struct passed is vector or array");
            }
            switch (type) {
                case BasicDataType.INTEGER:
                    return listOfObjects.stream().map(elem -> (Integer) elem).collect(Collectors.toList());
                case BasicDataType.LONG:
                    return listOfObjects.stream().map(elem -> (Long) elem).collect(Collectors.toList());
                case BasicDataType.FLOAT:
                case BasicDataType.DOUBLE:
                    return listOfObjects.stream().map(elem -> (Double) elem).collect(Collectors.toList());
                case BasicDataType.BOOLEAN:
                    return listOfObjects.stream().map(elem -> (Boolean) elem).collect(Collectors.toList());
                case BasicDataType.BYTE:
                    return listOfObjects.stream().map(elem -> (Byte) elem).collect(Collectors.toList());
                case BasicDataType.SHORT:
                    return listOfObjects.stream().map(elem -> (Short) elem).collect(Collectors.toList());
                case BasicDataType.STRING:
                    return listOfObjects.stream().map(elem -> (String) elem).collect(Collectors.toList());
                default:
                    throw new IllegalArgumentException("Given type is not supported");
            }

        }
    }

    @VisibleForTesting
    protected DataType convertInputToMLeapInputType(final String type, final String structure) {
        BasicType basicType;
        switch (type) {
            case BasicDataType.INTEGER:
                basicType = support.createInt();
                break;
            case BasicDataType.LONG:
                basicType = support.createLong();
                break;
            case BasicDataType.FLOAT:
                basicType = support.createFloat();
                break;
            case BasicDataType.DOUBLE:
                basicType = support.createDouble();
                break;
            case BasicDataType.BOOLEAN:
                basicType = support.createBoolean();
                break;
            case BasicDataType.BYTE:
                basicType = support.createByte();
                break;
            case BasicDataType.SHORT:
                basicType = support.createShort();
                break;
            case BasicDataType.STRING:
                basicType = support.createString();
                break;
            default:
                basicType = null;
        }
        if (basicType == null) {
            throw new IllegalArgumentException("Data type passed in the request is wrong for one or more columns");
        }
        if (StringUtils.isNotBlank(structure)) {
            switch (structure) {
                case DataStructureType.VECTOR:
                    return new TensorType(basicType, true);
                case DataStructureType.ARRAY:
                    return new ListType(basicType, true);
                case DataStructureType.BASIC:
                    return new ScalarType(basicType, true);
            }
        }
        // if structure field is not passed, it is by default basic
        return new ScalarType(basicType, true);

    }
}
