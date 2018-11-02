package com.amazonaws.sagemaker.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.amazonaws.sagemaker.dto.BatchExecutionParameter;
import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.serde.ResponseSerializer;
import com.amazonaws.sagemaker.type.StructureType;
import com.amazonaws.sagemaker.typeconverter.DataTypeConverter;
import com.amazonaws.sagemaker.utils.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Iterator;
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
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.collection.JavaConverters;
import scala.collection.Seq;

@RestController
public class ServingController {

    private static final Logger LOG = LoggerFactory.getLogger(ServingController.class);

    private final Transformer mleapTransformer;
    private final ResponseSerializer responseSerializer;
    private final DataTypeConverter typeConverter;


    @Autowired
    public ServingController(@NonNull Transformer mleapTransformer, @NonNull ResponseSerializer responseSerializer,
        @NonNull DataTypeConverter typeConverter) {
        this.mleapTransformer = mleapTransformer;
        this.responseSerializer = responseSerializer;
        this.typeConverter = typeConverter;
    }

    @RequestMapping(path = "/ping", method = GET)
    public ResponseEntity performShallowHealthCheck() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/execution-parameters", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity returnBatchExecutionParameter() {
        final BatchExecutionParameter batchParam = new BatchExecutionParameter(CommonUtils.getNumberOfThreads(),
            "SINGLE_RECORD", 5);
        try {
            final String responseStr = new ObjectMapper().writeValueAsString(batchParam);
            return ResponseEntity.ok(responseStr);
        } catch (JsonProcessingException jse) {
            LOG.error("Error in producing JSON value for {}", batchParam, jse);
        }
        return null;
    }

    @RequestMapping(path = "/invocations", method = POST)
    public ResponseEntity<String> transformRequest(@RequestBody SageMakerRequestObject sro,
        @RequestHeader(HttpHeaders.ACCEPT) String accept) {
        try {
            final DefaultLeapFrame dlf = typeConverter.convertInputToLeapFrame(sro);
            final Seq<String> predictionColumnSelectionArgs =
                JavaConverters.asScalaIteratorConverter(Collections.singletonList(sro.getOutput().getName()).iterator())
                    .asScala().toSeq();

            // Making call to the MLeap executor to get the output
            final DefaultLeapFrame predictions =
                mleapTransformer.transform(dlf).get().select(predictionColumnSelectionArgs).get();
            final Seq<Row> predictionRows = predictions.collect();
            final Iterable<Row> predictionRowsIterable = JavaConverters.asJavaIterableConverter(predictionRows)
                .asJava();

            // We are only querying for a single data point, so the iterator would have only one row
            ArrayRow predictionRow = (ArrayRow) (predictionRowsIterable.iterator().next());
            // If structure is not specified, it is assumed as basic data type
            if (StringUtils.isEmpty(sro.getOutput().getStructure()) || StringUtils
                .equals(sro.getOutput().getStructure(), StructureType.BASIC)) {
                final Object output = typeConverter
                    .castMLeapBasicTypeToJavaType(predictionRow, sro.getOutput().getType());
                return (output != null) ? responseSerializer.returnSingleOutput(output.toString(), accept) : null;
            } else {
                final Iterator<Object> responseIterator;
                if (StringUtils.equalsIgnoreCase(sro.getOutput().getStructure(), StructureType.VECTOR)) {
                    responseIterator =
                        JavaConverters.asJavaIteratorConverter(predictionRow.getTensor(0).rawValuesIterator()).asJava();
                } else {
                    responseIterator = predictionRow.getList(0).iterator();
                }
                return (responseIterator != null) ? responseSerializer.returnListOutput(responseIterator, accept)
                    : null;
            }
        } catch (final Exception ex) {
            LOG.error("Error in transforming input : {}", sro, ex);
            return CommonUtils.throwBadRequest("Error in transforming given input");
        }
    }


}
