package com.amazonaws.sagemaker.serde;

import com.amazonaws.sagemaker.dto.StandardJsonOutput;
import com.amazonaws.sagemaker.dto.TextJsonOutput;
import com.amazonaws.sagemaker.type.AdditionalMimeType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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

    public ResponseEntity<String> sendResponseForSingleValue(final String value, String acceptVal) {
        if (StringUtils.isEmpty(acceptVal)) {
            acceptVal = AdditionalMimeType.TEXT_CSV.toString();
        }
        return StringUtils.equals(acceptVal, AdditionalMimeType.TEXT_CSV.toString()) ? this.getCsvOkResponse(value)
            : this.getJsonlinesOkResponse(value);
    }

    public ResponseEntity<String> sendResponseForList(final Iterator<Object> outputDataIterator, String acceptVal)
        throws JsonProcessingException {
        if (StringUtils.equals(acceptVal, AdditionalMimeType.APPLICATION_JSONLINES.toString())) {
            return this.buildStandardJsonOutputForList(outputDataIterator);
        } else if (StringUtils.equals(acceptVal, AdditionalMimeType.APPLICATION_JSONLINES_TEXT.toString())) {
            return this.buildTextJsonOutputForList(outputDataIterator);
        } else {
            return this.buildCsvOutputForList(outputDataIterator);
        }
    }

    private ResponseEntity<String> buildCsvOutputForList(final Iterator<Object> outputDataIterator) {
        final StringJoiner sj = new StringJoiner(",");
        while (outputDataIterator.hasNext()) {
            sj.add(outputDataIterator.next().toString());
        }
        return this.getCsvOkResponse(sj.toString());
    }

    private ResponseEntity<String> buildStandardJsonOutputForList(final Iterator<Object> outputDataIterator)
        throws JsonProcessingException {
        final List<Object> columns = Lists.newArrayList();
        while (outputDataIterator.hasNext()) {
            columns.add(outputDataIterator.next());
        }
        final StandardJsonOutput jsonOutput = new StandardJsonOutput(columns);
        final String jsonRepresentation = mapper.writeValueAsString(jsonOutput);
        return this.getJsonlinesOkResponse(jsonRepresentation);
    }

    private ResponseEntity<String> buildTextJsonOutputForList(final Iterator<Object> outputDataIterator)
        throws JsonProcessingException {
        final StringJoiner stringJoiner = new StringJoiner(" ");
        while (outputDataIterator.hasNext()) {
            stringJoiner.add(outputDataIterator.next().toString());
        }
        final TextJsonOutput jsonOutput = new TextJsonOutput(stringJoiner.toString());
        final String jsonRepresentation = mapper.writeValueAsString(jsonOutput);
        return this.getJsonlinesOkResponse(jsonRepresentation);
    }

    private ResponseEntity<String> getCsvOkResponse(final String responseBody) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, AdditionalMimeType.TEXT_CSV.toString());
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

    // We are always responding with the valid format for application/jsonlines, which is a valid JSON
    private ResponseEntity<String> getJsonlinesOkResponse(final String responseBody) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, AdditionalMimeType.APPLICATION_JSONLINES.toString());
        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

}
