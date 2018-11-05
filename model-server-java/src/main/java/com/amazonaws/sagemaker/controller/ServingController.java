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
import com.amazonaws.sagemaker.utils.ScalaAbstractionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @RequestMapping(path = "/ping", method = GET)
    public ResponseEntity performShallowHealthCheck() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/execution-parameters", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity returnBatchExecutionParameter() throws JsonProcessingException {
        final BatchExecutionParameter batchParam = new BatchExecutionParameter(CommonUtils.getNumberOfThreads(1),
            "SINGLE_RECORD", 5);
        final String responseStr = new ObjectMapper().writeValueAsString(batchParam);
        return ResponseEntity.ok(responseStr);
    }

    @RequestMapping(path = "/invocations", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transformRequest(@RequestBody SageMakerRequestObject sro,
        @RequestHeader(HttpHeaders.ACCEPT) String accept) {
        if (sro == null) {
            return ResponseEntity.noContent().build();
        }
        try {
            this.retrieveAndVerifyAccept(accept);
            final DefaultLeapFrame dlf = typeConverter.castInputToLeapFrame(sro);

            // Making call to the MLeap executor to get the output
            final DefaultLeapFrame totalLeapFrame = ScalaAbstractionUtils.transformLeapFrame(mleapTransformer, dlf);
            final DefaultLeapFrame predictionsLeapFrame = ScalaAbstractionUtils
                .selectFromLeapFrame(totalLeapFrame, sro.getOutput().getName());
            final ArrayRow outputArrayRow = ScalaAbstractionUtils.getOutputArrayRow(predictionsLeapFrame);
            return transformToHttpResponse(sro, outputArrayRow, accept);

        } catch (final Exception ex) {
            LOG.error("Error in processing current request", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private void retrieveAndVerifyAccept(final String acceptFromRequest) {
        String acceptVal = StringUtils.isNotBlank(acceptFromRequest) ? acceptFromRequest
            : System.getenv("DEFAULT_INVOKE_ENDPOINT_ACCEPT");
        if (StringUtils.isNotEmpty(acceptFromRequest) && !VALID_ACCEPT_LIST.contains(acceptVal)) {
            throw new RuntimeException("Accept value passed via request or environment variable is not valid");
        }
    }

    private ResponseEntity<String> transformToHttpResponse(final SageMakerRequestObject sro,
        final ArrayRow predictionRow, final String accept) throws JsonProcessingException {

        if (StringUtils.equals(sro.getOutput().getStructure(), StructureType.BASIC)) {
            final Object output = typeConverter.castMLeapBasicTypeToJavaType(predictionRow, sro.getOutput().getType());
            return responseSerializer.sendResponseForSingleValue(output.toString(), accept);
        } else {
            // If not basic type, it can be vector or array type from Spark
            return responseSerializer.sendResponseForList(
                ScalaAbstractionUtils.getJavaObjectIteratorFromArrayRow(predictionRow, sro.getOutput().getStructure()),
                accept);
        }
    }


}
