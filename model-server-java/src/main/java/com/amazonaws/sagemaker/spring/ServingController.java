package com.amazonaws.sagemaker.spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ml.combust.mleap.json.DefaultFrameReader;
import ml.combust.mleap.json.DefaultFrameWriter;
import ml.combust.mleap.runtime.MleapContext;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.javadsl.BundleBuilder;
import ml.combust.mleap.runtime.javadsl.ContextBuilder;
import ml.combust.mleap.runtime.serialization.FrameWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringJoiner;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class ServingController {
    private Transformer pipelineTransformer;
    private Charset jsonCharset;
    private ObjectMapper mapper;
    private DefaultFrameReader frameReader;

    public ServingController() {
        MleapContext mleapContext = new ContextBuilder().createMleapContext();
        BundleBuilder bundleBuilder = new BundleBuilder();
        String MODEL_PATH = "/tmp/model";
        pipelineTransformer = bundleBuilder.load(new File(MODEL_PATH), mleapContext).root();
        jsonCharset = Charset.forName("UTF-8");
        mapper = new ObjectMapper();
        frameReader = new DefaultFrameReader();
    }

    @RequestMapping(path = "/invocations", method = POST, consumes = "application/json", produces = "text/csv")
    public Object invocations(@RequestBody byte[] request) throws Exception {
        String schemaJson = new String(request);
        JsonNode actualObj = mapper.readTree(schemaJson);
        String outputColumn = actualObj.get("output_col").asText();
        DefaultLeapFrame dlf = getLeapFrameFromJson(schemaJson);
        Seq<String> predictionColumnSelectionArgs =
                JavaConverters.asScalaIteratorConverter(Collections.singletonList(outputColumn).iterator()).asScala().toSeq();
        DefaultLeapFrame predictions =
                pipelineTransformer.transform(dlf).get().select(predictionColumnSelectionArgs).get();
        FrameWriter frameWriter = new DefaultFrameWriter(predictions);
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
                Iterator<Object> iterator_0 =
                        JavaConverters.asJavaIteratorConverter(arrayRow.getTensor(0).rawValuesIterator()).asJava();
                while (iterator_0.hasNext()) {
                    sj.add(iterator_0.next().toString());
                }
            }
            return sj.toString().getBytes(jsonCharset);
        }
    }

    private DefaultLeapFrame getLeapFrameFromJson(String frameJson) {
        byte[] frameBytes = frameJson.getBytes(jsonCharset);
        return frameReader.fromBytes(frameBytes, jsonCharset).get();
    }
}
