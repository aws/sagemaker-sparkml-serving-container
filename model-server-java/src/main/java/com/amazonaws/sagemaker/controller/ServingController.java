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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import scala.collection.JavaConverters;
import scala.collection.Seq;

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
    public ResponseEntity returnBatchExecutionParameter() {
        final BatchExecutionParameter batchParam = new BatchExecutionParameter(CommonUtils.getNumberOfThreads(1),
            "SINGLE_RECORD", 5);
        try {
            final String responseStr = new ObjectMapper().writeValueAsString(batchParam);
            return ResponseEntity.ok(responseStr);
        } catch (JsonProcessingException jse) {
            LOG.error("Error in producing JSON value for {}", batchParam, jse);
        }
        return null;
    }

    @RequestMapping(path = "/invocations", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transformRequest(@RequestBody SageMakerRequestObject sro,
        @RequestHeader(HttpHeaders.ACCEPT) String accept) {
        if (sro == null) {
            return ResponseEntity.noContent().build();
        }
        try {
            this.verifyAccept(accept);
            final DefaultLeapFrame dlf = typeConverter.convertInputToLeapFrame(sro);
            final Seq<String> predictionColumnSelectionArgs = JavaConverters
                .asScalaIteratorConverter(Collections.singletonList(sro.getOutput().getName()).iterator()).asScala()
                .toSeq();

            // Making call to the MLeap executor to get the output
            final DefaultLeapFrame predictions = mleapTransformer.transform(dlf).get()
                .select(predictionColumnSelectionArgs).get();
            final Iterator<Row> predictionRowsIterable = JavaConverters.asJavaIterableConverter(predictions.collect())
                .asJava().iterator();
            return transformToHttpResponse(sro, predictionRowsIterable, accept);

        } catch (final Exception ex) {
            LOG.error("Error in transforming input : {}", sro, ex);
            return CommonUtils.throwBadRequest(ex.getMessage());
        }
    }

    private void verifyAccept(final String acceptVal) {
        if ((StringUtils.isNotEmpty(acceptVal)) && !(VALID_ACCEPT_LIST.contains(acceptVal))) {
            throw new RuntimeException("Accept value is not valid");
        }
    }

    private ResponseEntity<String> transformToHttpResponse(final SageMakerRequestObject sro,
        final Iterator<Row> predictionRowsIterable, final String accept) throws JsonProcessingException {
        if (Iterators.size(predictionRowsIterable) == 0) {
            throw new RuntimeException("MLeap transformer did not produce any result");
        }
        // SageMaker input structure only allows to call MLeap transformer for single data point
        ArrayRow predictionRow = (ArrayRow) (predictionRowsIterable.next());
        if (StringUtils.equals(sro.getOutput().getStructure(), StructureType.BASIC)) {
            final Object output = typeConverter.castMLeapBasicTypeToJavaType(predictionRow, sro.getOutput().getType());
            return (output != null) ? responseSerializer.sendResponseForSingleValue(output.toString(), accept) : null;
        } else {
            // If not basic type, it can be vector or array type from Spark
            final Iterator<Object> responseIterator =
                (StringUtils.equals(sro.getOutput().getStructure(), StructureType.VECTOR)) ? JavaConverters
                    .asJavaIteratorConverter(predictionRow.getTensor(0).rawValuesIterator()).asJava()
                    : predictionRow.getList(0).iterator();

            return (responseIterator != null) ? responseSerializer.sendResponseForList(responseIterator, accept) : null;
        }
    }


}
