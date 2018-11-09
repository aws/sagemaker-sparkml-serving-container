package com.amazonaws.sagemaker.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.amazonaws.sagemaker.converter.DataTypeConverter;
import com.amazonaws.sagemaker.dto.BatchExecutionParameter;
import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.serde.ResponseSerializer;
import com.amazonaws.sagemaker.type.AdditionalMimeType;
import com.amazonaws.sagemaker.type.StructureType;
import com.amazonaws.sagemaker.utils.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
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
        .newArrayList(AdditionalMimeType.TEXT_CSV.toString(), AdditionalMimeType.APPLICATION_JSONLINES.toString(),
            AdditionalMimeType.APPLICATION_JSONLINES_TEXT.toString());

    private final Transformer mleapTransformer;
    private final ResponseSerializer responseSerializer;
    private final DataTypeConverter typeConverter;

    @Autowired
    public ServingController(Transformer mleapTransformer, ResponseSerializer responseSerializer,
        DataTypeConverter typeConverter) {
        this.mleapTransformer = Preconditions.checkNotNull(mleapTransformer);
        this.responseSerializer = Preconditions.checkNotNull(responseSerializer);
        this.typeConverter = Preconditions.checkNotNull(typeConverter);
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
        final BatchExecutionParameter batchParam = new BatchExecutionParameter(CommonUtils.getNumberOfThreads(1),
            "SINGLE_RECORD", 5);
        final String responseStr = new ObjectMapper().writeValueAsString(batchParam);
        return ResponseEntity.ok(responseStr);
    }

    /**
     * Implements the invocations POST API
     *
     * @param sro, the request object
     * @param accept, accept parameter from request
     * @return ResponseEntity with body as the expected payload JSON & proper statuscode based on the input
     */
    @RequestMapping(path = "/invocations", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transformRequest(@RequestBody SageMakerRequestObject sro,
        @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept) {
        if (sro == null) {
            return ResponseEntity.noContent().build();
        }
        try {
            final String acceptVal = this.retrieveAndVerifyAccept(accept);
            final DefaultLeapFrame dlf = typeConverter.castInputToLeapFrame(sro);

            // Making call to the MLeap executor to get the output
            final DefaultLeapFrame totalLeapFrame = CommonUtils.transformLeapFrame(mleapTransformer, dlf);
            final DefaultLeapFrame predictionsLeapFrame = CommonUtils
                .selectFromLeapFrame(totalLeapFrame, sro.getOutput().getName());
            final ArrayRow outputArrayRow = CommonUtils.getOutputArrayRow(predictionsLeapFrame);
            return transformToHttpResponse(sro, outputArrayRow, acceptVal);

        } catch (final Exception ex) {
            LOG.error("Error in processing current request", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @VisibleForTesting
    protected String retrieveAndVerifyAccept(final String acceptFromRequest) {
        final String acceptVal = checkEmptyAccept(acceptFromRequest) ? CommonUtils
            .getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT") : acceptFromRequest;
        if (StringUtils.isNotEmpty(acceptVal) && !VALID_ACCEPT_LIST.contains(acceptVal)) {
            throw new IllegalArgumentException("Accept value passed via request or environment variable is not valid");
        }
        return StringUtils.isNotEmpty(acceptVal) ? acceptVal : AdditionalMimeType.TEXT_CSV.toString();
    }

    /**
     * Spring sends the Accept as "*\/*" (star/star) in case accept is not passed via request
     */
    private boolean checkEmptyAccept(final String acceptFromRequest) {
        return (StringUtils.isBlank(acceptFromRequest) || StringUtils.equals(acceptFromRequest, MediaType.ALL_VALUE));
    }

    private ResponseEntity<String> transformToHttpResponse(final SageMakerRequestObject sro,
        final ArrayRow predictionRow, final String accept) throws JsonProcessingException {

        if (StringUtils.equals(sro.getOutput().getStructure(), StructureType.BASIC)) {
            final Object output = typeConverter.castMLeapBasicTypeToJavaType(predictionRow, sro.getOutput().getType());
            return responseSerializer.sendResponseForSingleValue(output.toString(), accept);
        } else {
            // If not basic type, it can be vector or array type from Spark
            return responseSerializer.sendResponseForList(
                CommonUtils.getJavaObjectIteratorFromArrayRow(predictionRow, sro.getOutput().getStructure()), accept);
        }
    }


}
