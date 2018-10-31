package com.amazonaws.sagemaker.controller;

import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.dto.SingleColumn;
import com.fasterxml.jackson.databind.ObjectMapper;
import ml.combust.mleap.core.types.DataType;
import ml.combust.mleap.core.types.ScalarType;
import ml.combust.mleap.core.types.StructField;
import ml.combust.mleap.core.types.StructType;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class ServingController {
    private final LeapFrameBuilder leapFrameBuilder;
    private final LeapFrameBuilderSupport support;
    private final Transformer pipelineTransformer;
    private final Charset jsonCharset;

    @Autowired
    public ServingController(LeapFrameBuilder leapFrameBuilder, LeapFrameBuilderSupport support,
                             Transformer pipelineTransformer, Charset jsonCharset,
                             ObjectMapper mapper) {
        this.leapFrameBuilder = leapFrameBuilder;
        this.support = support;
        this.pipelineTransformer = pipelineTransformer;
        this.jsonCharset = jsonCharset;
    }

    @RequestMapping(path = "/ping", method = GET)
    public ResponseEntity performShallowHealthCheck() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/invocations", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            "text/csv")
    public byte[] transformRequest(@RequestBody SageMakerRequestObject sro) {
        String outputColumn = sro.getOutput().getName();
        DefaultLeapFrame dlf = convert(sro);
        Seq<String> predictionColumnSelectionArgs =
                JavaConverters.asScalaIteratorConverter(Collections.singletonList(outputColumn).iterator()).asScala().toSeq();
        DefaultLeapFrame predictions =
                pipelineTransformer.transform(dlf).get().select(predictionColumnSelectionArgs).get();
        Seq<Row> predictionRows = predictions.collect();
        Iterable<Row> predictionRowsIterable =
                JavaConverters.asJavaIterableConverter(predictionRows).asJava();

        if (StringUtils.equals(outputColumn, "prediction")) {
            StringJoiner sj = new StringJoiner("\n");
            for (Row row : predictionRowsIterable) {
                ArrayRow arrayRow = (ArrayRow) row;
                Double val = arrayRow.getDouble(0);
                sj.add(val.toString());
            }
            return sj.toString().getBytes(jsonCharset);
        } else {
            StringJoiner sj = new StringJoiner(",");
            for (Row row : predictionRowsIterable) {
                ArrayRow arrayRow = (ArrayRow) row;
                Iterator<Object> it =
                        JavaConverters.asJavaIteratorConverter(arrayRow.getTensor(0).rawValuesIterator()).asJava();
                while (it.hasNext()) {
                    sj.add(it.next().toString());
                }
            }
            return sj.toString().getBytes(jsonCharset);
        }
    }

    private DefaultLeapFrame convert(final SageMakerRequestObject sro) {
        List<StructField> structFieldList = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (SingleColumn sc : sro.getInput()) {
            structFieldList.add(new StructField(sc.getName(), getDataType(sc.getType())));
            values.add(getValue(sc.getType(), sc.getVal()));
        }

        StructType schema = leapFrameBuilder.createSchema(structFieldList);
        Row currentRow = support.createRowFromIterable(values);

        List<Row> rows = new ArrayList<>();
        rows.add(currentRow);

        return leapFrameBuilder.createFrame(schema, rows);
    }

    private Object getValue(String type, String value) {
        switch (type) {
            case "int":
                return new Integer(value);
            case "float":
                return new Float(value);
            case "double":
                return new Double(value);
            case "string":
                return value;
            default:
                return null;
        }

    }

    private DataType getDataType(String type) {
        switch (type) {
            case "int":
                return new ScalarType(support.createInt(), true);
            case "float":
                return new ScalarType(support.createFloat(), true);
            case "double":
                return new ScalarType(support.createDouble(), true);
            case "string":
                return new ScalarType(support.createString(), true);
            default:
                return null;
        }
    }

}
