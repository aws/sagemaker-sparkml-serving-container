package com.amazonaws.sagemaker.controller;

import com.amazonaws.sagemaker.converter.DataTypeConverter;
import com.amazonaws.sagemaker.dto.BatchExecutionParameter;
import com.amazonaws.sagemaker.dto.SageMakerRequestObject;
import com.amazonaws.sagemaker.dto.SingleColumn;
import com.amazonaws.sagemaker.serde.ResponseSerializer;
import com.amazonaws.sagemaker.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommonUtils.class)
class ServingControllerTest {

    private ServingController controller;
    private ResponseSerializer serializer;
    private DataTypeConverter converter;
    private Transformer mleapTransformerMock;
    private SageMakerRequestObject sro;
    private DefaultLeapFrame responseLeapFrame;
    private ArrayRow outputArrayRow;
    List<SingleColumn> input;

    //PowerMock needs zero arugment constructor
    public ServingControllerTest() {
    }

    private void buildDefaultSageMakerRequestObject() {
        input = Lists.newArrayList(new SingleColumn("test_name_1", "int", null, new Integer("1")),
            new SingleColumn("test_name_2", "double", null, new Double("2.0")));
        SingleColumn output = new SingleColumn("out_name", "int", null, null);
        sro = new SageMakerRequestObject(input, output);
    }


    private void buildResponseLeapFrame() {
        responseLeapFrame = new DataTypeConverter(new LeapFrameBuilderSupport(), new LeapFrameBuilder())
            .castInputToLeapFrame(sro);
        outputArrayRow = new ArrayRow(Lists.newArrayList(new Integer("1")));
    }

    @Before
    public void setup() {
        converter = new DataTypeConverter(new LeapFrameBuilderSupport(), new LeapFrameBuilder());
        serializer = new ResponseSerializer();
        mleapTransformerMock = Mockito.mock(Transformer.class);
        this.buildDefaultSageMakerRequestObject();
        this.buildResponseLeapFrame();
        controller = new ServingController(mleapTransformerMock, serializer, converter);
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito
            .when(CommonUtils.transformLeapFrame(Mockito.any(Transformer.class), Mockito.any(DefaultLeapFrame.class)))
            .thenReturn(responseLeapFrame);
        PowerMockito.when(CommonUtils.selectFromLeapFrame(Mockito.any(DefaultLeapFrame.class), Mockito.anyString()))
            .thenReturn(responseLeapFrame);
        PowerMockito.when(CommonUtils.getOutputArrayRow(Mockito.any(DefaultLeapFrame.class)))
            .thenReturn(outputArrayRow);
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
        Assert.assertEquals((int) batchParam.getMaxConcurrentTransforms(), CommonUtils.getNumberOfThreads(1));
        Assert.assertEquals(batchParam.getBatchStrategy(), "SINGLE_RECORD");
        Assert.assertEquals((int) batchParam.getMaxPayloadInMB(), 5);
    }

    @Test
    public void testSingleValueResponse() {
        final ResponseEntity<String> output = controller.transformRequest(sro, "text/csv");
        Assert.assertEquals(output.getBody(), "1");
    }

    @Test
    public void testListValueResponse() {
        SingleColumn outputCol = new SingleColumn("out_name", "int", "array", null);
        sro = new SageMakerRequestObject(input, outputCol);
        List<Object> outputList = Lists.newArrayList(1, 2);
        PowerMockito
            .when(CommonUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputList.iterator());
        final ResponseEntity<String> output = controller.transformRequest(sro, "text/csv");
        Assert.assertEquals(output.getBody(), "1,2");
    }

    @Test
    public void testListValueJsonLinesResponse() {
        SingleColumn outputCol = new SingleColumn("out_name", "int", "vector", null);
        sro = new SageMakerRequestObject(input, outputCol);
        List<Object> outputList = Lists.newArrayList(1, 2);
        PowerMockito
            .when(CommonUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputList.iterator());
        final ResponseEntity<String> output = controller.transformRequest(sro, "application/jsonlines");
        Assert.assertEquals(output.getBody(), "{\"features\":[1,2]}");
    }

    @Test
    public void testListValueNoAcceptResponse() {
        SingleColumn outputCol = new SingleColumn("out_name", "int", "array", null);
        sro = new SageMakerRequestObject(input, outputCol);
        List<Object> outputList = Lists.newArrayList(1, 2);
        PowerMockito
            .when(CommonUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenReturn(outputList.iterator());
        final ResponseEntity<String> output = controller.transformRequest(sro, null);
        Assert.assertEquals(output.getBody(), "1,2");
    }

    @Test
    public void testListValueThrowsException() {
        SingleColumn outputCol = new SingleColumn("out_name", "int", "array", null);
        sro = new SageMakerRequestObject(input, outputCol);
        PowerMockito
            .when(CommonUtils.getJavaObjectIteratorFromArrayRow(Mockito.any(ArrayRow.class), Mockito.anyString()))
            .thenThrow(new RuntimeException("input data is not valid"));
        final ResponseEntity<String> output = controller.transformRequest(sro, "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(output.getBody(), "input data is not valid");
    }

    @Test
    public void testInputNull() {
        final ResponseEntity<String> output = controller.transformRequest(null, "text/csv");
        Assert.assertEquals(output.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    public void testParseAcceptEmptyFromRequestEnvironmentPresent() {
        PowerMockito.when(CommonUtils.getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT"))
            .thenReturn("application/jsonlines;data=text");
        Assert.assertEquals(controller.retrieveAndVerifyAccept(null), "application/jsonlines;data=text");
    }

    @Test
    public void testParseAcceptAnyFromRequestEnvironmentPresent() {
        PowerMockito.when(CommonUtils.getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT"))
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
        PowerMockito.when(CommonUtils.getEnvironmentVariable("SAGEMAKER_DEFAULT_INVOCATIONS_ACCEPT"))
            .thenReturn("application/json");
        controller.retrieveAndVerifyAccept("application/json");
    }


}
