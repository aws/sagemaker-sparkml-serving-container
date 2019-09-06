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

package com.amazonaws.sagemaker.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.amazonaws.sagemaker.dto.BatchExecutionParameter;
import com.amazonaws.sagemaker.dto.DataSchema;
import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.helper.DataConversionHelper;
import com.amazonaws.sagemaker.helper.ResponseHelper;
import com.amazonaws.sagemaker.type.AdditionalMediaType;
import com.amazonaws.sagemaker.type.DataStructureType;
import com.amazonaws.sagemaker.utils.ScalaUtils;
import com.amazonaws.sagemaker.utils.SystemUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.frame.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Spring controller class which implements the APIs
 */
@RestController
public class ServingController {

    private static final Logger LOG = LoggerFactory.getLogger(ServingController.class);
    private static final List<String> VALID_ACCEPT_LIST = Lists
        .newArrayList(AdditionalMediaType.TEXT_CSV_VALUE, AdditionalMediaType.APPLICATION_JSONLINES_VALUE,
            AdditionalMediaType.APPLICATION_JSONLINES_TEXT_VALUE);

    private final Transformer mleapTransformer;
    private final ResponseHelper responseHelper;
    private final DataConversionHelper dataConversionHelper;
    private final ObjectMapper mapper;

    @Autowired
    public ServingController(final Transformer mleapTransformer, final ResponseHelper responseHelper,
        final DataConversionHelper dataConversionHelper, final ObjectMapper mapper) {
        this.mleapTransformer = Preconditions.checkNotNull(mleapTransformer);
        this.responseHelper = Preconditions.checkNotNull(responseHelper);
        this.dataConversionHelper = Preconditions.checkNotNull(dataConversionHelper);
        this.mapper = Preconditions.checkNotNull(mapper);
    }

    /**
     * Implements the health check GET API
     *
     * @return ResponseEntity with status 200
     */
    @RequestMapping(path = "/ping", method = GET)
    public ResponseEntity performShallowHealthCheck() {
        return ResponseEntity.ok().build();
    }

    /**
     * Implements the Batch Execution GET Parameter API
     *
     * @return ResponseEntity with body as the expected payload JSON & status 200
     */
    @RequestMapping(path = "/execution-parameters", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity returnBatchExecutionParameter() throws JsonProcessingException {
        final BatchExecutionParameter batchParam = new BatchExecutionParameter(SystemUtils.getNumberOfThreads(1),
            "SINGLE_RECORD", 5);
        final String responseStr = mapper.writeValueAsString(batchParam);
        return ResponseEntity.ok(responseStr);
    }

    /**
     * Implements the invocations POST API for application/json input
     *
     * @param sro, the request object
     * @param accept, accept parameter from request
     * @return ResponseEntity with body as the expected payload JSON & proper statuscode based on the input
     */
    @RequestMapping(path = "/invocations", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transformRequestJson(@RequestBody final SageMakerRequestObject sro,
        @RequestHeader(value = HttpHeaders.ACCEPT, required = false) final String accept) {
        if (sro == null) {
            LOG.error("Input passed to the request is empty");
            return ResponseEntity.noContent().build();
        }
        try {
            final String acceptVal = this.retrieveAndVerifyAccept(accept);
            final DataSchema schema = this.retrieveAndVerifySchema(sro.getSchema(), mapper);
            return this.processInputData(Collections.singletonList(sro.getData()), schema, acceptVal);
        } catch (final Exception ex) {
            LOG.error("Error in processing current request", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Implements the invocations POST API for application/json input
     *
     * @param csvRow, data in row format in CSV
     * @param accept, accept parameter from request
     * @return ResponseEntity with body as the expected payload JSON & proper statuscode based on the input
     */
    @RequestMapping(path = "/invocations", method = POST, consumes = AdditionalMediaType.TEXT_CSV_VALUE)
    public ResponseEntity<String> transformRequestCsv(@RequestBody final byte[] csvRow,
        @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept) {
        if (csvRow == null) {
            LOG.error("Input passed to the request is empty");
            return ResponseEntity.noContent().build();
        }
        try {
            final String acceptVal = this.retrieveAndVerifyAccept(accept);
            final DataSchema schema = this.retrieveAndVerifySchema(null, mapper);
            return this
                .processInputData(dataConversionHelper.convertCsvToObjectList(new String(csvRow), schema), schema,
                    acceptVal);
        } catch (final Exception ex) {
            LOG.error("Error in processing current request", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @VisibleForTesting
    protected String retrieveAndVerifyAccept(final String acceptFromRequest) {
        final String acceptVal = checkEmptyAccept(acceptFromRequest) ? SystemUtils
            .getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT") : acceptFromRequest;
        if (StringUtils.isNotEmpty(acceptVal) && !VALID_ACCEPT_LIST.contains(acceptVal)) {
            throw new IllegalArgumentException("Accept value passed via request or environment variable is not valid");
        }
        return StringUtils.isNotEmpty(acceptVal) ? acceptVal : AdditionalMediaType.TEXT_CSV_VALUE;
    }

    @VisibleForTesting
    protected DataSchema retrieveAndVerifySchema(final DataSchema schemaFromPayload, final ObjectMapper mapper)
        throws IOException {
        if ((schemaFromPayload == null) && (SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA") == null)) {
            throw new RuntimeException(
                "Input schema has to be provided either via environment variable or " + "via the request");
        }
        return (schemaFromPayload != null) ? schemaFromPayload
            : mapper.readValue(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA"), DataSchema.class);
    }

    private ResponseEntity<String> processInputData(final List<List<Object>> inputDatas, final DataSchema schema,
        final String acceptVal) throws JsonProcessingException {
        final DefaultLeapFrame leapFrame = dataConversionHelper.convertInputToLeapFrame(schema, inputDatas);
        // Making call to the MLeap executor to get the output
        final DefaultLeapFrame totalLeapFrame = ScalaUtils.transformLeapFrame(mleapTransformer, leapFrame);
        final DefaultLeapFrame predictionsLeapFrame = ScalaUtils
            .selectFromLeapFrame(totalLeapFrame, schema.getOutput().getName());
        final List<Row> outputArrayRow = ScalaUtils.getOutputArrayRow(predictionsLeapFrame);
        return transformToHttpResponse(schema, outputArrayRow, acceptVal);

    }

    private boolean checkEmptyAccept(final String acceptFromRequest) {
        //Spring may send the Accept as "*\/*" (star/star) in case accept is not passed via request
        return (StringUtils.isBlank(acceptFromRequest) || StringUtils.equals(acceptFromRequest, MediaType.ALL_VALUE));
    }

    private ResponseEntity<String> transformToHttpResponse(final DataSchema schema, final List<Row> predictionsRow,
        final String accept) throws JsonProcessingException {

        if (StringUtils.equals(schema.getOutput().getStruct(), DataStructureType.BASIC)) {
            final Object output = dataConversionHelper
                .convertMLeapBasicTypeToJavaType(predictionsRow.get(0), schema.getOutput().getType());
            return responseHelper.sendResponseForSingleValue(output.toString(), accept);
        } else {
            // If not basic type, it can be vector or array type from Spark
            return responseHelper.sendResponseForList(
                    predictionsRow.stream().map(predictionRow -> ScalaUtils.getJavaObjectIteratorFromArrayRow(predictionRow, schema.getOutput().getStruct())).collect(Collectors.toList())
                , accept);
        }
    }

}
