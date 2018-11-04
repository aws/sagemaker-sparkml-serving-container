package com.amazonaws.sagemaker.configuration;

import java.nio.file.NoSuchFileException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;

public class DependencyConfigurationTest {

    private DependencyConfiguration configuration;

    @Before
    public void setup() {
        configuration = new DependencyConfiguration();
    }

    @Test
    public void testModelLocationNotNull() {
        Assert.assertNotNull(configuration.provideModelLocation());
        Assert.assertEquals(configuration.provideModelLocation(), "/opt/ml/model");
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

    //We expect the test to fail with an error that the model artifact file is not present, not before that
    @Test (expected= NoSuchFileException.class)
    public void testTransformerNotNull() {
        Assert.assertNotNull(configuration
            .provideTransformer(configuration.provideModelLocation(), configuration.provideBundleBuilder(),
                configuration.provideMleapContext(configuration.provideContextBuilder())));
    }

    @Test
    public void testJettyServletWebServerFactoryNotNull() {
        JettyServletWebServerFactory jettyServletTest = configuration.provideJettyServletWebServerFactory();
        final String listenerPort =
            (System.getenv("SAGEMAKER_BIND_TO_PORT") != null) ? System.getenv("SAGEMAKER_BIND_TO_PORT")
                : "8080";
        Assert.assertEquals((int) new Integer(listenerPort), jettyServletTest.getPort());
        Assert.assertNotNull(jettyServletTest.getServerCustomizers());
    }

}
