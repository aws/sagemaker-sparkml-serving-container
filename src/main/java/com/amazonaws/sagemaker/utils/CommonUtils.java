package com.amazonaws.sagemaker.utils;

import com.amazonaws.sagemaker.type.StructureType;
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
 * Utility class for dealing with Scala to Java conversion related issues and dealing with Environment variable
 * retrieval. These functionalities are moved to this class so that they can be easily mocked out by PowerMockito
 * .mockStatic while testing the actual classes.
 */
public class CommonUtils {

    /**
     * Computes the number of threads to set based on number of available processors in the host
     * @param coreToThreadRatio, the multiplicative factor
     * @return coreToThreadRation multiplied by available processors in the host
     */
    public static int getNumberOfThreads(final Integer coreToThreadRatio) {
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        return coreToThreadRatio * numberOfCores;
    }

    /**
     * Retrieves environment variable pertaining to a key
     * @param key, the environment variable key
     * @return the value corresponding to the key from environment settings
     */
    public static String getEnvironmentVariable(final String key) {
        return System.getenv(key);
    }

    /**
     * Invokes MLeap transformer object with DefaultLeapFrame and returns DefaultLeapFrame from MLeap response Try Monad
     * @param transformer, the MLeap transformer which performs the inference
     * @param leapFrame, input to MLeap
     * @return the DefaultLeapFrame in response
     */
    public static DefaultLeapFrame transformLeapFrame(final Transformer transformer, final DefaultLeapFrame leapFrame) {
        return transformer.transform(leapFrame).get();
    }

    /**
     * Selects a value corresponding to a key from DefaultLeapFrame and returns DefaultLeapFrame from MLeap response
     * Try Monad
     * @param key, the value corresponding to key to be retrieved
     * @param leapFrame, input to MLeap
     * @return the DefaultLeapFrame in response
     */
    public static DefaultLeapFrame selectFromLeapFrame(final DefaultLeapFrame leapFrame, final String key) {
        final Seq<String> predictionColumnSelectionArgs = JavaConverters
            .asScalaIteratorConverter(Collections.singletonList(key).iterator()).asScala().toSeq();
        return leapFrame.select(predictionColumnSelectionArgs).get();
    }

    /**
     * Returns an ArrayRow object from DefaultLeapFrame Try Monad after converting Scala collections to Java
     * collections
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
     * @param predictionRow, the output ArrayRow
     * @param structure, whether it is Spark Vector or Array
     * @return Iterator to raw values of the Vector or Array
     */
    public static Iterator<Object> getJavaObjectIteratorFromArrayRow(final ArrayRow predictionRow,
        final String structure) {
        return (StringUtils.equals(structure, StructureType.VECTOR)) ? JavaConverters
            .asJavaIteratorConverter(predictionRow.getTensor(0).rawValuesIterator()).asJava()
            : predictionRow.getList(0).iterator();
    }

}
