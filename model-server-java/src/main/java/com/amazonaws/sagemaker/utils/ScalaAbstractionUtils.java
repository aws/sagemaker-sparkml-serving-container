package com.amazonaws.sagemaker.utils;

import com.amazonaws.sagemaker.type.StructureType;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Iterator;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.frame.Transformer;
import org.apache.commons.lang3.StringUtils;
import scala.collection.JavaConverters;
import scala.collection.Seq;

public class ScalaAbstractionUtils {

    public static DefaultLeapFrame transformLeapFrame(final Transformer transformer, final DefaultLeapFrame leapFrame) {
        return transformer.transform(leapFrame).get();
    }

    public static DefaultLeapFrame selectFromLeapFrame(final DefaultLeapFrame leapFrame, String key) {
        final Seq<String> predictionColumnSelectionArgs = JavaConverters
            .asScalaIteratorConverter(Collections.singletonList(key).iterator()).asScala().toSeq();
        return leapFrame.select(predictionColumnSelectionArgs).get();
    }

    public static ArrayRow getOutputArrayRow(final DefaultLeapFrame leapFrame) {
        final Iterator<Row> rowIterator = JavaConverters.asJavaIterableConverter(leapFrame.collect()).asJava()
            .iterator();
        if (Iterators.size(rowIterator) == 0) {
            throw new RuntimeException("MLeap transformer did not produce any result");
        }
        // SageMaker input structure only allows to call MLeap transformer for single data point
        return (ArrayRow) (rowIterator.next());
    }

    public static Iterator<Object> getJavaObjectIteratorFromArrayRow(final ArrayRow predictionRow,
        final String structure) {
        return (StringUtils.equals(structure, StructureType.VECTOR)) ? JavaConverters
            .asJavaIteratorConverter(predictionRow.getTensor(0).rawValuesIterator()).asJava()
            : predictionRow.getList(0).iterator();
    }

}
