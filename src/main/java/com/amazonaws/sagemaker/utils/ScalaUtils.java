package com.amazonaws.sagemaker.utils;

import com.amazonaws.sagemaker.type.DataStructureType;
import java.util.Collections;
import java.util.Iterator;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.frame.Transformer;
import org.apache.commons.lang3.StringUtils;
import scala.collection.JavaConverters;
import scala.collection.Seq;

/**
 * Utility class for dealing with Scala to Java conversion related issues. These functionalities are moved to this
 * class so that they can be easily mocked out by PowerMockito.mockStatic while testing the actual classes.
 */
public class ScalaUtils {

    /**
     * Invokes MLeap transformer object with DefaultLeapFrame and returns DefaultLeapFrame from MLeap helper Try Monad
     *
     * @param transformer, the MLeap transformer which performs the inference
     * @param leapFrame, input to MLeap
     * @return the DefaultLeapFrame in helper
     */
    public static DefaultLeapFrame transformLeapFrame(final Transformer transformer, final DefaultLeapFrame leapFrame) {
        return transformer.transform(leapFrame).get();
    }

    /**
     * Selects a value corresponding to a key from DefaultLeapFrame and returns DefaultLeapFrame from MLeap helper Try
     * Monad
     *
     * @param key, the value corresponding to key to be retrieved
     * @param leapFrame, input to MLeap
     * @return the DefaultLeapFrame in helper
     */
    public static DefaultLeapFrame selectFromLeapFrame(final DefaultLeapFrame leapFrame, final String key) {
        final Seq<String> predictionColumnSelectionArgs = JavaConverters
            .asScalaIteratorConverter(Collections.singletonList(key).iterator()).asScala().toSeq();
        return leapFrame.select(predictionColumnSelectionArgs).get();
    }

    /**
     * Returns an ArrayRow object from DefaultLeapFrame Try Monad after converting Scala collections to Java
     * collections
     *
     * @param leapFrame, the DefaultLeapFrame from which output to be extracted
     * @return ArrayRow which can be used to retrieve the original output
     */
    public static ArrayRow getOutputArrayRow(final DefaultLeapFrame leapFrame) {
        final Iterator<Row> rowIterator = JavaConverters.asJavaIterableConverter(leapFrame.collect()).asJava()
            .iterator();
        // SageMaker input structure only allows to call MLeap transformer for single data point
        return (ArrayRow) (rowIterator.next());
    }

    /**
     * Retrieves the raw output value from ArrayRow for Vector/Array use cases.
     *
     * @param predictionRow, the output ArrayRow
     * @param structure, whether it is Spark Vector or Array
     * @return Iterator to raw values of the Vector or Array
     */
    public static Iterator<Object> getJavaObjectIteratorFromArrayRow(final ArrayRow predictionRow,
        final String structure) {
        return (StringUtils.equals(structure, DataStructureType.VECTOR)) ? JavaConverters
            .asJavaIteratorConverter(predictionRow.getTensor(0).toDense().rawValuesIterator()).asJava()
            : predictionRow.getList(0).iterator();
    }

}
