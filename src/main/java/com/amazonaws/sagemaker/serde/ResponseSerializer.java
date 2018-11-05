package com.amazonaws.sagemaker.serde;

import com.amazonaws.sagemaker.dto.StandardJsonlinesOutput;
import com.amazonaws.sagemaker.dto.TextJsonlinesOutput;
import com.amazonaws.sagemaker.type.AdditionalMimeType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * This class contains the logic for converting MLeap response into SageMaker specific response along with status-codes
 */
@Component
public class ResponseSerializer {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Sends the response when the output is a single value (e.g. prediction)
     *
     * @param value, the response value
     * @param acceptVal, the accept customer has passed or default (text/csv) if not passed
     * @return Spring ResponseEntity which contains the body and the header
     */
    public ResponseEntity<String> sendResponseForSingleValue(final String value, String acceptVal) {
        if (StringUtils.isEmpty(acceptVal)) {
            acceptVal = AdditionalMimeType.TEXT_CSV.toString();
        }
        return StringUtils.equals(acceptVal, AdditionalMimeType.TEXT_CSV.toString()) ? this.getCsvOkResponse(value)
            : this.getJsonlinesOkResponse(value);
    }

    /**
     * This method is responsible for sending the values in the appropriate format so that it can be parsed by other 1P
     * algorithms. Currently, it supports two formats, standard jsonlines and jsonlines for text. Please see
     * test/resources/com/amazonaws/sagemaker/dto for example output format or SageMaker built-in algorithms
     * documentaiton to know about the output format.
     *
     * @param outputDataIterator, data iterator for raw output values in case output is an Array or Vector
     * @param acceptVal, the accept customer has passed or default (text/csv) if not passed
     * @return Spring ResponseEntity which contains the body and the header.
     */
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
        final StandardJsonlinesOutput jsonOutput = new StandardJsonlinesOutput(columns);
        final String jsonRepresentation = mapper.writeValueAsString(jsonOutput);
        return this.getJsonlinesOkResponse(jsonRepresentation);
    }

    private ResponseEntity<String> buildTextJsonOutputForList(final Iterator<Object> outputDataIterator)
        throws JsonProcessingException {
        final StringJoiner stringJoiner = new StringJoiner(" ");
        while (outputDataIterator.hasNext()) {
            stringJoiner.add(outputDataIterator.next().toString());
        }
        final TextJsonlinesOutput jsonOutput = new TextJsonlinesOutput(stringJoiner.toString());
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
