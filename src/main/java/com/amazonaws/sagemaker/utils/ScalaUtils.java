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

package com.amazonaws.sagemaker.utils;

import com.amazonaws.sagemaker.type.DataStructureType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.javadsl.LeapFrameSupport;
import org.apache.commons.lang3.StringUtils;
import scala.collection.JavaConverters;

/**
 * Utility class for dealing with Scala to Java conversion related issues. These functionalities are moved to this
 * class so that they can be easily mocked out by PowerMockito.mockStatic while testing the actual classes.
 */
public class ScalaUtils {

    private final static LeapFrameSupport leapFrameSupport = new LeapFrameSupport();

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
        return leapFrameSupport.select(leapFrame, Collections.singletonList(key));
    }

    /**
     * Returns an ArrayRow object from DefaultLeapFrame Try Monad after converting Scala collections to Java
     * collections
     *
     * @param leapFrame, the DefaultLeapFrame from which output to be extracted
     * @return ArrayRow which can be used to retrieve the original output
     */
    public static List<Row> getOutputArrayRow(final DefaultLeapFrame leapFrame) {
        return leapFrameSupport.collect(leapFrame);
    }

    /**
     * Retrieves the raw output value from ArrayRow for Vector/Array use cases.
     *
     * @param predictionRow, the output ArrayRow
     * @param structure, whether it is Spark Vector or Array
     * @return Iterator to raw values of the Vector or Array
     */
    public static Iterator<Object> getJavaObjectIteratorFromArrayRow(final Row predictionRow,
        final String structure) {
        return (StringUtils.equals(structure, DataStructureType.VECTOR)) ? JavaConverters
            .asJavaIteratorConverter(predictionRow.getTensor(0).toDense().rawValuesIterator()).asJava()
            : predictionRow.getList(0).iterator();
    }

}
