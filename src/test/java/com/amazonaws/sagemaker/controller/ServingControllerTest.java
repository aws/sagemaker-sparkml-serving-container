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

import com.amazonaws.sagemaker.dto.BatchExecutionParameter;
import com.amazonaws.sagemaker.dto.ColumnSchema;
import com.amazonaws.sagemaker.dto.DataSchema;
import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.helper.DataConversionHelper;
import com.amazonaws.sagemaker.helper.ResponseHelper;
import com.amazonaws.sagemaker.type.AdditionalMediaType;
import com.amazonaws.sagemaker.utils.ScalaUtils;
import com.amazonaws.sagemaker.utils.SystemUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ScalaUtils.class, SystemUtils.class})
public class ServingControllerTest {

    private ServingController controller;
    private DataConversionHelper converter = new DataConversionHelper(new LeapFrameBuilderSupport(),
        new LeapFrameBuilder());
    private Transformer mleapTransformerMock;
    private SageMakerRequestObject sro;
    private DefaultLeapFrame responseLeapFrame;
    private ArrayRow outputArrayRow;
    private List<ColumnSchema> inputColumns;
    private ColumnSchema outputColumn;
    private List<Object> inputData;
    private String schemaInJson;
    private ObjectMapper mapper = new ObjectMapper();
    private ResponseHelper responseHelper = new ResponseHelper(mapper);

    //PowerMock needs zero arugment constructor
    public ServingControllerTest() {
    }

    private void buildDefaultSageMakerRequestObject() {
        schemaInJson = "{\"input\":[{\"name\":\"test_name_1\",\"type\":\"int\"},{\"name\":\"test_name_2\","
            + "\"type\":\"double\"}],\"output\":{\"name\":\"out_name\",\"type\":\"int\"}}";
        inputColumns = Lists.newArrayList(new ColumnSchema("test_name_1", "int", null),
            new ColumnSchema("test_name_2", "double", null));
        outputColumn = new ColumnSchema("out_name", "int", null);
        inputData = Lists.newArrayList(new Integer("1"), new Double("2.0"));
        sro = new SageMakerRequestObject(new DataSchema(inputColumns, outputColumn), inputData);
    }


    private void buildResponseLeapFrame() {
        responseLeapFrame = new DataConversionHelper(new LeapFrameBuilderSupport(), new LeapFrameBuilder())
            .convertInputToLeapFrame(sro.getSchema(), sro.getData());
        outputArrayRow = new ArrayRow(Lists.newArrayList(new Integer("1")));
    }

    @Before
    public void setup() {
        responseHelper = new ResponseHelper(mapper);
        mleapTransformerMock = Mockito.mock(Transformer.class);
        this.buildDefaultSageMakerRequestObject();
        this.buildResponseLeapFrame();
        controller = new ServingController(mleapTransformerMock, responseHelper, converter, mapper);
        PowerMockito.mockStatic(ScalaUtils.class);
        PowerMockito.mockStatic(SystemUtils.class);
        PowerMockito
            .when(ScalaUtils.transformLeapFrame(Mockito.any(Transformer.class), Mockito.any(DefaultLeapFrame.class)))
            .thenReturn(responseLeapFrame);
        PowerMockito.when(ScalaUtils.selectFromLeapFrame(Mockito.any(DefaultLeapFrame.class), Mockito.anyString()))
            .thenReturn(responseLeapFrame);
        PowerMockito.when(ScalaUtils.getOutputArrayRow(Mockito.any(DefaultLeapFrame.class))).thenReturn(outputArrayRow);
    }

    @Test
    public void testPerformShallowHealthCheck() {
        Assert.assertEquals(controller.performShallowHealthCheck().getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testReturnBatchExecutionParameter() throws Exception {
        ResponseEntity response = controller.returnBatchExecutionParameter();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        BatchExecutionParameter batchParam = new ObjectMapper()
            .readValue(Objects.requireNonNull(response.getBody()).toString(), BatchExecutionParameter.class);
        Assert.assertEquals((int) batchParam.getMaxConcurrentTransforms(), SystemUtils.getNumberOfThreads(1));
        Assert.assertEquals(batchParam.getBatchStrategy(), "SINGLE_RECORD");
        Assert.assertEquals((int) batchParam.getMaxPayloadInMB(), 5);
    }

    @Test
    public void testSingleValueCsvAcceptResponse() {
        final ResponseEntity<String> output = controller.transformRequestJson(sro, AdditionalMediaType.TEXT_CSV_VALUE);
        Assert.assertEquals(output.getBody(), "1");
        Assert.assertEquals(Objects.requireNonNull(output.getHeaders().getContentType()).toString(),
            AdditionalMediaType.TEXT_CSV_VALUE);
    }

    @Test
    public void testSingleValueJsonlinesAcceptResponse() {
        final ResponseEntity<String> output = controller
            .transformRequestJson(sro, AdditionalMediaType.APPLICATION_JSONLINES_VALUE);
        Assert.assertEquals(output.getBody(), "1");
        Assert.assertEquals(Objects.requireNonNull(output.getHeaders().getContentType()).toString(),
            AdditionalMediaType.APPLICATION_JSONLINES_VALUE);
    }

    @Test
    public void testSingleValueNoAcceptResponse() {
        final ResponseEntity<String> output = controller.transformRequestJson(sro, null);
        Assert.assertEquals(output.getBody(), "1");
        Assert.assertEquals(Objects.requireNonNull(output.getHeaders().getContentType()).toString(),
            AdditionalMediaType.TEXT_CSV_VALUE);
    }

    @Test
    public void testListValueCsvAcceptResponse() {
        outputColumn = new ColumnSchema("out_name", "int", "array");
        List<Object> outputResponse = Lists.newArrayList(1, 2);
        sro = new SageMakerRequestObject(new DataSchema(inputColumns, outputColumn), inputData);
        PowerMockito
            .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputResponse.iterator());
        final ResponseEntity<String> output = controller.transformRequestJson(sro, "text/csv");
        Assert.assertEquals(output.getBody(), "1,2");
    }

    @Test
    public void testListValueJsonLinesAcceptResponse() {
        outputColumn = new ColumnSchema("out_name", "int", "vector");
        List<Object> outputResponse = Lists.newArrayList(1, 2);
        sro = new SageMakerRequestObject(new DataSchema(inputColumns, outputColumn), inputData);
        PowerMockito
            .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputResponse.iterator());
        final ResponseEntity<String> output = controller.transformRequestJson(sro, "application/jsonlines");
        Assert.assertEquals(output.getBody(), "{\"features\":[1,2]}");
    }

    @Test
    public void testListValueNoAcceptResponse() {
        outputColumn = new ColumnSchema("out_name", "int", "array");
        List<Object> outputResponse = Lists.newArrayList(1, 2);
        sro = new SageMakerRequestObject(new DataSchema(inputColumns, outputColumn), inputData);
        PowerMockito
            .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputResponse.iterator());
        final ResponseEntity<String> output = controller.transformRequestJson(sro, null);
        Assert.assertEquals(output.getBody(), "1,2");
    }

    @Test
    public void testListValueMLeapThrowsException() {
        outputColumn = new ColumnSchema("out_name", "int", "array");
        sro = new SageMakerRequestObject(new DataSchema(inputColumns, outputColumn), inputData);
        PowerMockito
            .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenThrow(new RuntimeException("input data is not valid"));
        final ResponseEntity<String> output = controller.transformRequestJson(sro, "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(output.getBody(), "input data is not valid");
    }

    @Test
    public void testInputNull() {
        final ResponseEntity<String> output = controller.transformRequestJson(null, "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    public void testCsvApiWithListInput() {
        schemaInJson = "{\"input\":[{\"name\":\"test_name_1\",\"type\":\"int\"},{\"name\":\"test_name_2\","
            + "\"type\":\"double\"}],\"output\":{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}}";
        List<Object> outputResponse = Lists.newArrayList(1, 2);
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        PowerMockito
            .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputResponse.iterator());
        final ResponseEntity<String> output = controller.transformRequestCsv("1,2.0".getBytes(), "text/csv");
        Assert.assertEquals(output.getBody(), "1,2");
    }

    @Test
    public void testCsvApiWithNullInput() {
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        final ResponseEntity<String> output = controller.transformRequestCsv(null, "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    public void testListValueMLeapThrowsExceptionCsvApi() {
        schemaInJson = "{\"input\":[{\"name\":\"test_name_1\",\"type\":\"int\"},{\"name\":\"test_name_2\","
            + "\"type\":\"double\"}],\"output\":{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}}";
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        PowerMockito
            .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenThrow(new RuntimeException("input data is not valid"));
        final ResponseEntity<String> output = controller.transformRequestCsv("1,2.0".getBytes(), "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(output.getBody(), "input data is not valid");
    }

    @Test
    public void testJsonLinesApiWithListInputCsvOutput() {
        schemaInJson = "{"
                + "\"input\":["
                + "{\"name\":\"test_name_1\",\"type\":\"int\"},"
                + "{\"name\":\"test_name_2\",\"type\":\"double\"},"
                + "{\"name\":\"test_name_3\",\"type\":\"string\"}"
                + "],"
                + "\"output\":"
                + "{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}"
                + "}";

        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA"))
                .thenReturn(schemaInJson);

        List<Object> outputResponseForFirstInput = Lists.newArrayList(1, 2);
        List<Object> outputResponseForSecondInput = Lists.newArrayList(3, 4);
        List<Object> outputResponseForThirdInput = Lists.newArrayList(5, 6);
        List<Object> outputResponseForFourthInput = Lists.newArrayList(7, 8);
        List<Object> outputResponseForFifthInput = Lists.newArrayList(9, 10);
        List<Object> outputResponseForSixthInput = Lists.newArrayList(11, 12);
        List<Object> outputResponseForSeventhInput = Lists.newArrayList(13, 14);
        List<Object> outputResponseForEighthInput = Lists.newArrayList(15, 16);

        PowerMockito
                .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
                .thenReturn(outputResponseForFirstInput.iterator())
                .thenReturn(outputResponseForSecondInput.iterator())
                .thenReturn(outputResponseForThirdInput.iterator())
                .thenReturn(outputResponseForFourthInput.iterator())
                .thenReturn(outputResponseForFifthInput.iterator())
                .thenReturn(outputResponseForSixthInput.iterator())
                .thenReturn(outputResponseForSeventhInput.iterator())
                .thenReturn(outputResponseForEighthInput.iterator());

        final String expectOutput = "[[1,2], [3,4]]";

        final ResponseEntity<String> output =
                controller.transformRequestJsonLines(
                        "{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}".getBytes(), "text/csv");

        Assert.assertEquals(expectOutput, output.getBody());

        final String expectOutput1 = "[[5,6], [7,8]]";

        final ResponseEntity<String> output1 =
                controller.transformRequestJsonLines(
                        "{\"data\":[1,2.0,\"TEST1\"]}\n{\"data\":[2,3.0,\"TEST\"]}".getBytes(), "text/csv");

        Assert.assertEquals(expectOutput1, output1.getBody());

        final String expectOutput2 = "[[9,10], [11,12], [13,14], [15,16]]";

        final ResponseEntity<String> output2 =
                controller.transformRequestJsonLines(
                        ("{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}\n"
                                + "{\"data\":[1,2.0,\"TEST1\"]}\n"
                                + "{\"data\":[2,3.0,\"TEST\"]}"
                        ).getBytes(),
                        "text/csv");

        Assert.assertEquals(expectOutput2, output2.getBody());
    }

    @Test
    public void testJsonLinesApiWithListInputJsonOutput() {
        schemaInJson = "{"
                + "\"input\":["
                + "{\"name\":\"test_name_1\",\"type\":\"int\"},"
                + "{\"name\":\"test_name_2\",\"type\":\"double\"},"
                + "{\"name\":\"test_name_3\",\"type\":\"string\"}"
                + "],"
                + "\"output\":"
                + "{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}"
                + "}";

        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA"))
                .thenReturn(schemaInJson);

        List<Object> outputResponseForFirstInput = Lists.newArrayList(1, 2);
        List<Object> outputResponseForSecondInput = Lists.newArrayList(3, 4);
        List<Object> outputResponseForThirdInput = Lists.newArrayList(5, 6);
        List<Object> outputResponseForFourthInput = Lists.newArrayList(7, 8);
        List<Object> outputResponseForFifthInput = Lists.newArrayList(9, 10);
        List<Object> outputResponseForSixthInput = Lists.newArrayList(11, 12);
        List<Object> outputResponseForSeventhInput = Lists.newArrayList(13, 14);
        List<Object> outputResponseForEighthInput = Lists.newArrayList(15, 16);

        PowerMockito
                .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
                .thenReturn(outputResponseForFirstInput.iterator())
                .thenReturn(outputResponseForSecondInput.iterator())
                .thenReturn(outputResponseForThirdInput.iterator())
                .thenReturn(outputResponseForFourthInput.iterator())
                .thenReturn(outputResponseForFifthInput.iterator())
                .thenReturn(outputResponseForSixthInput.iterator())
                .thenReturn(outputResponseForSeventhInput.iterator())
                .thenReturn(outputResponseForEighthInput.iterator());

        final String expectOutput = "[[{\"features\":[1,2]}], [{\"features\":[3,4]}]]";

        final ResponseEntity<String> output =
                controller.transformRequestJsonLines(
                        "{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}".getBytes(),
                        "application/jsonlines");

        Assert.assertEquals(expectOutput, output.getBody());

        final String expectOutput1 = "[[{\"features\":[5,6]}], [{\"features\":[7,8]}]]";

        final ResponseEntity<String> output1 =
                controller.transformRequestJsonLines(
                        "{\"data\":[1,2.0,\"TEST1\"]}\n{\"data\":[2,3.0,\"TEST\"]}".getBytes(),
                        "application/jsonlines");

        Assert.assertEquals(expectOutput1, output1.getBody());

        final String expectOutput2 =
                "[[{\"features\":[9,10]}], [{\"features\":[11,12]}], "
                        + "[{\"features\":[13,14]}], [{\"features\":[15,16]}]]";

        final ResponseEntity<String> output2 =
                controller.transformRequestJsonLines(
                        ("{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}\n"
                                + "{\"data\":[1,2.0,\"TEST1\"]}\n"
                                + "{\"data\":[2,3.0,\"TEST\"]}"
                        ).getBytes(),
                        "application/jsonlines");

        Assert.assertEquals(expectOutput2, output2.getBody());
    }

    @Test
    public void testJsonLinesApiWithListInputJsonTextOutput() {
        schemaInJson = "{"
                + "\"input\":["
                + "{\"name\":\"test_name_1\",\"type\":\"int\"},"
                + "{\"name\":\"test_name_2\",\"type\":\"double\"},"
                + "{\"name\":\"test_name_3\",\"type\":\"string\"}"
                + "],"
                + "\"output\":"
                + "{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}"
                + "}";

        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA"))
                .thenReturn(schemaInJson);

        List<Object> outputResponseForFirstInput = Lists.newArrayList(1, 2);
        List<Object> outputResponseForSecondInput = Lists.newArrayList(3, 4);
        List<Object> outputResponseForThirdInput = Lists.newArrayList(5, 6);
        List<Object> outputResponseForFourthInput = Lists.newArrayList(7, 8);
        List<Object> outputResponseForFifthInput = Lists.newArrayList(9, 10);
        List<Object> outputResponseForSixthInput = Lists.newArrayList(11, 12);
        List<Object> outputResponseForSeventhInput = Lists.newArrayList(13, 14);
        List<Object> outputResponseForEighthInput = Lists.newArrayList(15, 16);

        PowerMockito
                .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
                .thenReturn(outputResponseForFirstInput.iterator())
                .thenReturn(outputResponseForSecondInput.iterator())
                .thenReturn(outputResponseForThirdInput.iterator())
                .thenReturn(outputResponseForFourthInput.iterator())
                .thenReturn(outputResponseForFifthInput.iterator())
                .thenReturn(outputResponseForSixthInput.iterator())
                .thenReturn(outputResponseForSeventhInput.iterator())
                .thenReturn(outputResponseForEighthInput.iterator());

        final String expectOutput = "[[{\"source\":\"1 2\"}], [{\"source\":\"3 4\"}]]";

        final ResponseEntity<String> output =
                controller.transformRequestJsonLines(
                        "{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}".getBytes(),
                        "application/jsonlines;data=text");

        Assert.assertEquals(expectOutput, output.getBody());

        final String expectOutput1 = "[[{\"source\":\"5 6\"}], [{\"source\":\"7 8\"}]]";

        final ResponseEntity<String> output1 =
                controller.transformRequestJsonLines(
                        "{\"data\":[1,2.0,\"TEST1\"]}\n{\"data\":[2,3.0,\"TEST\"]}".getBytes(),
                        "application/jsonlines;data=text");

        Assert.assertEquals(expectOutput1, output1.getBody());

        final String expectOutput2 =
                "[[{\"source\":\"9 10\"}], [{\"source\":\"11 12\"}], "
                        + "[{\"source\":\"13 14\"}], [{\"source\":\"15 16\"}]]";

        final ResponseEntity<String> output2 =
                controller.transformRequestJsonLines(
                        ("{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}\n{\"data\":[1,2.0,\"TEST1\"]}\n{\"data\":[2,3.0,\"TEST\"]}"
                        ).getBytes(),
                        "application/jsonlines;data=text");

        Assert.assertEquals(expectOutput2, output2.getBody());
    }

    @Test
    public void testProcessInputDataForJsonLines() throws IOException {

        String jsonLinesAsString =
                "{\"schema\":"
                        + "{\"input\":[{\"name\":\"test_name_1\",\"type\":\"int\"},{\"name\":\"test_name_2\","
                        + "\"type\":\"double\"},{\"name\":\"test_name_3\",\"type\":\"string\"}],"
                        + "\"output\":{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}},"
                        + "\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}"
                        + "\n{\"data\":[1,2.0,\"TEST1\"]}"
                        + "\n{\"data\":[2,3.0,\"TEST\"]}";

        List<Object> outputResponseForFirstInput = Lists.newArrayList(1, 2);
        List<Object> outputResponseForSecondInput = Lists.newArrayList(3, 4);
        List<Object> outputResponseForThirdInput = Lists.newArrayList(5, 6);
        List<Object> outputResponseForFourthInput = Lists.newArrayList(7, 8);

        PowerMockito
                .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
                .thenReturn(outputResponseForFirstInput.iterator())
                .thenReturn(outputResponseForSecondInput.iterator())
                .thenReturn(outputResponseForThirdInput.iterator())
                .thenReturn(outputResponseForFourthInput.iterator());

        final String expectOutput = "[[1,2], [3,4], [5,6], [7,8]]";
        final ResponseEntity<String> output = controller.transformRequestJsonLines(
                jsonLinesAsString.getBytes(), "text/csv");

        Assert.assertEquals(expectOutput, output.getBody());
    }

    @Test
    public void testJsonLinesApiWithListInputThrowsException() {
        schemaInJson = "{\"input\":[{\"name\":\"test_name_1\",\"type\":\"int\"},{\"name\":\"test_name_2\","
                + "\"type\":\"double\"},{\"name\":\"test_name_3\",\"type\":\"string\"}],\"output\":{\"name\":\"out_name\",\"type\":\"int\",\"struct\":\"vector\"}}";
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        PowerMockito
                .when(ScalaUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
                .thenThrow(new RuntimeException("input data is not valid"));
        final ResponseEntity<String> output = controller.transformRequestJsonLines("{\"data\":[[1,2.0,\"TEST1\"], [2,3.0,\"TEST\"]]}".getBytes(), "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(output.getBody(), "input data is not valid");
    }

    @Test
    public void testJsonLinesApiWithNullInput() {
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        final ResponseEntity<String> output = controller.transformRequestJsonLines(null, "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testJsonLinesApiWithEmptyInput() {
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        final ResponseEntity<String> output = controller.transformRequestJsonLines(new byte[0], "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    public void testParseAcceptEmptyFromRequestEnvironmentPresent() {
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT"))
            .thenReturn("application/jsonlines;data=text");
        Assert.assertEquals(controller.retrieveAndVerifyAccept(null), "application/jsonlines;data=text");
    }

    @Test
    public void testParseAcceptAnyFromRequestEnvironmentPresent() {
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT"))
            .thenReturn("application/jsonlines;data=text");
        Assert.assertEquals(controller.retrieveAndVerifyAccept("*/*"), "application/jsonlines;data=text");
    }

    @Test
    public void testParseAcceptEmptyFromRequestEnvironmentNotPresent() {
        Assert.assertEquals(controller.retrieveAndVerifyAccept(null), "text/csv");
    }

    @Test
    public void testParseAcceptAnyFromRequestEnvironmentNotPresent() {
        Assert.assertEquals(controller.retrieveAndVerifyAccept("*/*"), "text/csv");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAcceptInEnvironment() {
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT"))
            .thenReturn("application/json");
        controller.retrieveAndVerifyAccept("application/json");
    }

    @Test
    public void testSchemaPresentInRequestAndEnvironment() throws IOException {
        inputColumns = Lists.newArrayList(new ColumnSchema("name_1", "type_1", "struct_1"),
            new ColumnSchema("name_2", "type_2", "struct_2"));
        outputColumn = new ColumnSchema("name_out_1", "type_out_1", "struct_out_1");
        DataSchema ds = new DataSchema(inputColumns, outputColumn);
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        DataSchema outputSchema = controller.retrieveAndVerifySchema(ds, mapper);
        Assert.assertEquals(outputSchema.getInput().size(), 2);
        Assert.assertEquals(outputSchema.getInput().get(0).getName(), "name_1");
        Assert.assertEquals(outputSchema.getOutput().getName(), "name_out_1");
    }

    @Test
    public void testSchemaPresentOnlyInEnvironment() throws IOException {
        schemaInJson = "{\"input\":[{\"name\":\"i_1\",\"type\":\"int\"}],\"output\":{\"name\":\"o_1\","
            + "\"type\":\"double\"}}";
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_SPARKML_SCHEMA")).thenReturn(schemaInJson);
        DataSchema outputSchema = controller.retrieveAndVerifySchema(null, mapper);
        Assert.assertEquals(outputSchema.getInput().size(), 1);
        Assert.assertEquals(outputSchema.getInput().get(0).getName(), "i_1");
        Assert.assertEquals(outputSchema.getOutput().getName(), "o_1");
    }

    @Test(expected = RuntimeException.class)
    public void testSchemaAbsentEverywhere() throws IOException {
        controller.retrieveAndVerifySchema(null, mapper);
    }


}
