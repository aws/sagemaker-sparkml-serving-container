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

package com.amazonaws.sagemaker.configuration;

import com.amazonaws.sagemaker.utils.SystemUtils;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SystemUtils.class)
public class BeanConfigurationTest {

    public BeanConfigurationTest() {
    }

    private BeanConfiguration configuration = new BeanConfiguration();

    @Test
    public void testModelLocationNotNull() {
        Assert.assertNotNull(configuration.provideModelFile());
        Assert.assertEquals(configuration.provideModelFile(), new File("/opt/ml/model"));
    }

    @Test
    public void testContextBuilderNotNull() {
        Assert.assertNotNull(configuration.provideContextBuilder());
    }

    @Test
    public void testBundleBuilderNotNull() {
        Assert.assertNotNull(configuration.provideBundleBuilder());
    }

    @Test
    public void testMleapContextNotNull() {
        Assert.assertNotNull(configuration.provideMleapContext(configuration.provideContextBuilder()));
    }

    @Test
    public void testLeapFrameBuilderNotNull() {
        Assert.assertNotNull(configuration.provideLeapFrameBuilder());
    }

    @Test
    public void testLeapFrameBuilderSupportNotNull() {
        Assert.assertNotNull(configuration.provideLeapFrameBuilderSupport());
    }

    @Test
    public void testTransformerNotNull() {
        File dummyMLeapFile = new File(this.getClass().getResource("model").getFile());
        Assert.assertNotNull(configuration.provideTransformer(dummyMLeapFile, configuration.provideBundleBuilder(),
            configuration.provideMleapContext(configuration.provideContextBuilder())));
    }

    @Test
    public void testObjectMapperNotNull() {
        Assert.assertNotNull(configuration.provideObjectMapper());
    }

    @Test
    public void testJettyServletWebServerFactoryNotNull() {
        JettyServletWebServerFactory jettyServletTest = configuration.provideJettyServletWebServerFactory();
        final String listenerPort =
            (System.getenv("SAGEMAKER_BIND_TO_PORT") != null) ? System.getenv("SAGEMAKER_BIND_TO_PORT") : "8080";
        Assert.assertEquals((int) new Integer(listenerPort), jettyServletTest.getPort());
        Assert.assertNotNull(jettyServletTest.getServerCustomizers());
    }

    @Test
    public void testParsePortFromEnvironment() {
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(SystemUtils.getEnvironmentVariable("SAGEMAKER_BIND_TO_PORT")).thenReturn("7070");
        Assert.assertEquals(configuration.getHttpListenerPort(), "7070");
    }

}
