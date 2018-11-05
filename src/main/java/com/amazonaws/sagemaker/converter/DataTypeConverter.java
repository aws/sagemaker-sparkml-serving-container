package com.amazonaws.sagemaker.converter;

import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.type.BasicDataType;
import com.amazonaws.sagemaker.type.StructureType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converter class to convert data between input to MLeap expected types and convert back MLeap response to Java types
 * for output.
 */
@Component
public class DataTypeConverter {

    private final LeapFrameBuilderSupport support;
    private final LeapFrameBuilder leapFrameBuilder;

    @Autowired
    public DataTypeConverter(LeapFrameBuilderSupport support, LeapFrameBuilder leapFrameBuilder) {
        this.support = Preconditions.checkNotNull(support);
        this.leapFrameBuilder = Preconditions.checkNotNull(leapFrameBuilder);
    }


    /**
     * Convert input object to DefaultLeapFrame
     * @param sro, the request object
     * @return the DefaultLeapFrame object which MLeap transformer expects
     */
    public DefaultLeapFrame castInputToLeapFrame(final SageMakerRequestObject sro) {

        final List<StructField> structFieldList = sro.getInput().stream()
            .map(sc -> new StructField(sc.getName(), this.castInputToMLeapInputType(sc.getType(), sc.getStructure())))
            .collect(Collectors.toList());
        final List<Object> valueList = sro.getInput().stream()
            .map(sc -> this.castInputToJavaType(sc.getType(), sc.getStructure(), sc.getVal()))
            .collect(Collectors.toList());

        final StructType schema = leapFrameBuilder.createSchema(structFieldList);
        final Row currentRow = support.createRowFromIterable(valueList);

        final List<Row> rows = Lists.newArrayList();
        rows.add(currentRow);

        return leapFrameBuilder.createFrame(schema, rows);
    }

    /**
     * Convert basic types in the MLeap response to Java types for output.
     * @param predictionRow, the ArrayRow from MLeapResponse
     * @param type, the basic type to which the response should be casted, provided by user via input
     * @return the proper Java type
     */
    public Object castMLeapBasicTypeToJavaType(final ArrayRow predictionRow, final String type) {
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
    protected Object castInputToJavaType(final String type, final String structure, final Object value) {
        if (StringUtils.isBlank(structure) || StringUtils.equals(structure, StructureType.BASIC)) {
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
    protected DataType castInputToMLeapInputType(final String type, final String structure) {
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
                case StructureType.VECTOR:
                    return new TensorType(basicType, true);
                case StructureType.ARRAY:
                    return new ListType(basicType, true);
                case StructureType.BASIC:
                    return new ScalarType(basicType, true);
            }
        }
        // if structure field is not passed, it is by default basic
        return new ScalarType(basicType, true);

    }
}
