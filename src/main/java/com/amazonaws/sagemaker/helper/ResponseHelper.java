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

import com.amazonaws.sagemaker.dto.JsonlinesStandardOutput;
import com.amazonaws.sagemaker.dto.JsonlinesTextOutput;
import com.amazonaws.sagemaker.type.AdditionalMediaType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * This class contains the logic for converting MLeap helper into SageMaker specific helper along with status-codes
 */
@Component
public class ResponseHelper {

    private final ObjectMapper mapper;

    @Autowired
    public ResponseHelper(final ObjectMapper mapper) {
        this.mapper = Preconditions.checkNotNull(mapper);
    }

    /**
     * Sends the helper when the output is a single value (e.g. prediction)
     *
     * @param value, the helper value
     * @param acceptVal, the accept customer has passed or default (text/csv) if not passed
     * @return Spring ResponseEntity which contains the body and the header
     */
    public ResponseEntity<String> sendResponseForSingleValue(final String value, String acceptVal) {
        if (StringUtils.isEmpty(acceptVal)) {
            acceptVal = AdditionalMediaType.TEXT_CSV_VALUE;
        }
        return StringUtils.equals(acceptVal, AdditionalMediaType.TEXT_CSV_VALUE) ? this.getCsvOkResponse(value)
            : this.getJsonlinesOkResponse(value);
    }

    /**
     * This method is responsible for sending the values in the appropriate format so that it can be parsed by other 1P
     * algorithms. Currently, it supports two formats, standard jsonlines and jsonlines for text. Please see
     * test/resources/com/amazonaws/sagemaker/dto for example output format or SageMaker built-in algorithms
     * documentaiton to know about the output format.
     *
     * @param outputDatasIterator, data iterator for raw output values in case output is an Array or Vector
     * @param acceptVal, the accept customer has passed or default (text/csv) if not passed
     * @return Spring ResponseEntity which contains the body and the header.
     */
    public ResponseEntity<String> sendResponseForList(final List<Iterator<Object>> outputDatasIterator, String acceptVal)
        throws JsonProcessingException {
        if (StringUtils.equals(acceptVal, AdditionalMediaType.APPLICATION_JSONLINES_VALUE)) {
            return this.buildStandardJsonOutputForList(outputDatasIterator.get(0));
        } else if (StringUtils.equals(acceptVal, AdditionalMediaType.APPLICATION_JSONLINES_TEXT_VALUE)) {
            return this.buildTextJsonOutputForList(outputDatasIterator.get(0));
        } else {
            return this.buildCsvOutputForList(outputDatasIterator);
        }
    }

    private ResponseEntity<String> buildCsvOutputForList(final List<Iterator<Object>> outputDatasIterator) {

        final StringJoiner sjLineBreaks = new StringJoiner("\n");

        for(Iterator<Object> outputDataIterator : outputDatasIterator)
        {
            final StringJoiner sj = new StringJoiner(",");
            while (outputDataIterator.hasNext()) {
                sj.add(outputDataIterator.next().toString());
            }
            sjLineBreaks.add(sj.toString());
        }

        return this.getCsvOkResponse(sjLineBreaks.toString());
    }

    private ResponseEntity<String> buildStandardJsonOutputForList(final Iterator<Object> outputDataIterator)
        throws JsonProcessingException {
        final List<Object> columns = Lists.newArrayList();
        while (outputDataIterator.hasNext()) {
            columns.add(outputDataIterator.next());
        }
        final JsonlinesStandardOutput jsonOutput = new JsonlinesStandardOutput(columns);
        final String jsonRepresentation = mapper.writeValueAsString(jsonOutput);
        return this.getJsonlinesOkResponse(jsonRepresentation);
    }

    private ResponseEntity<String> buildTextJsonOutputForList(final Iterator<Object> outputDataIterator)
        throws JsonProcessingException {
        final StringJoiner stringJoiner = new StringJoiner(" ");
        while (outputDataIterator.hasNext()) {
            stringJoiner.add(outputDataIterator.next().toString());
        }
        final JsonlinesTextOutput jsonOutput = new JsonlinesTextOutput(stringJoiner.toString());
        final String jsonRepresentation = mapper.writeValueAsString(jsonOutput);
        return this.getJsonlinesOkResponse(jsonRepresentation);
    }

    private ResponseEntity<String> getCsvOkResponse(final String responseBody) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, AdditionalMediaType.TEXT_CSV_VALUE);
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    // We are always responding with the valid format for application/jsonlines, whicth is a valid JSON
    private ResponseEntity<String> getJsonlinesOkResponse(final String responseBody) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, AdditionalMediaType.APPLICATION_JSONLINES_VALUE);
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

}
