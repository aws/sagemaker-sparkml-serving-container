package com.amazonaws.sagemaker.serde;

import com.amazonaws.sagemaker.dto.StandardJsonOutput;
import com.amazonaws.sagemaker.type.MimeType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseSerializer.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static boolean IS_BATCH_ENVIRONMENT = (System.getenv("SAGEMAKER_BATCH") != null);

    public ResponseEntity<String> returnSingleOutput(String value, String contentType) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        return ResponseEntity.ok().headers(headers).body(value);
    }

    public ResponseEntity<String> returnListOutput(Iterator<Object> outputDataIterator, String contentType) {
        if (StringUtils.equals(contentType, MimeType.TEXT_CSV)) {
            return this.returnCsvOutput(outputDataIterator);
        } else {
            return this.returnStandardJsonOutput(outputDataIterator, contentType);
        }
    }

    private ResponseEntity<String> returnCsvOutput(Iterator<Object> outputDataIterator) {
        final StringJoiner sj = new StringJoiner(",");
        while (outputDataIterator.hasNext()) {
            sj.add(outputDataIterator.next().toString());
        }
        return this.buildCsvOkResponse(sj.toString());
    }

    private ResponseEntity<String> buildJsonOkResponse(String responseBody, String jsonType) {
        final HttpHeaders headers = new HttpHeaders();
        final String contentType = (!IS_BATCH_ENVIRONMENT) ? jsonType : MimeType.APPLICATION_JSONLINES;
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    private ResponseEntity<String> buildCsvOkResponse(String responseBody) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MimeType.TEXT_CSV);
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    private ResponseEntity<String> returnStandardJsonOutput(Iterator<Object> outputDataIterator, String contentType) {
        final List<Object> columns = new ArrayList<>();
        while (outputDataIterator.hasNext()) {
            columns.add(outputDataIterator.next());
        }
        final StandardJsonOutput jsonOutput = new StandardJsonOutput(columns);
        try {
            final String jsonRepresentation = mapper.writeValueAsString(jsonOutput);
            return this.buildJsonOkResponse(jsonRepresentation, contentType);
        } catch (final JsonProcessingException ex) {
            LOG.error("Error in converting response to JSON format", ex);
            return ResponseEntity.badRequest().build();
        }
    }
}
