package com.amazonaws.sagemaker.configuration;

import com.amazonaws.sagemaker.utils.SystemUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import ml.combust.mleap.runtime.MleapContext;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.javadsl.BundleBuilder;
import ml.combust.mleap.runtime.javadsl.ContextBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * Contains all Spring bean configurations
 */
@SpringBootConfiguration
public class BeanConfiguration {

    private static final String DEFAULT_HTTP_LISTENER_PORT = "8080";
    private static final String DEFAULT_MODEL_LOCATION = "/opt/ml/model";
    private static final Integer MAX_CORE_TO_THREAD_RATIO = 10;
    private static final Integer MIN_CORE_TO_THREAD_RATIO = 2;

    @Bean
    public File provideModelFile() {
        return new File(DEFAULT_MODEL_LOCATION);
    }

    @Bean
    public ContextBuilder provideContextBuilder() {
        return new ContextBuilder();
    }

    @Bean
    public MleapContext provideMleapContext(ContextBuilder contextBuilder) {
        return contextBuilder.createMleapContext();
    }

    @Bean
    public BundleBuilder provideBundleBuilder() {
        return new BundleBuilder();
    }

    @Bean
    public LeapFrameBuilder provideLeapFrameBuilder() {
        return new LeapFrameBuilder();
    }

    @Bean
    public LeapFrameBuilderSupport provideLeapFrameBuilderSupport() {
        return new LeapFrameBuilderSupport();
    }

    @Bean
    public Transformer provideTransformer(final File modelFile, final BundleBuilder bundleBuilder,
        final MleapContext mleapContext) {
        return bundleBuilder.load(modelFile, mleapContext).root();
    }

    @Bean
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JettyServletWebServerFactory provideJettyServletWebServerFactory() {
        final JettyServletWebServerFactory jettyServlet = new JettyServletWebServerFactory(
            new Integer(this.getHttpListenerPort()));
        final List<JettyServerCustomizer> serverCustomizerList = Lists.newArrayList();
        final JettyServerCustomizer serverCustomizer = server -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMinThreads(SystemUtils.getNumberOfThreads(MIN_CORE_TO_THREAD_RATIO));
            threadPool.setMaxThreads(SystemUtils.getNumberOfThreads(MAX_CORE_TO_THREAD_RATIO));
        };
        serverCustomizerList.add(serverCustomizer);
        jettyServlet.setServerCustomizers(serverCustomizerList);
        return jettyServlet;
    }

    @VisibleForTesting
    protected String getHttpListenerPort() {
        return (SystemUtils.getEnvironmentVariable("SAGEMAKER_BIND_TO_PORT") != null) ? SystemUtils
            .getEnvironmentVariable("SAGEMAKER_BIND_TO_PORT") : DEFAULT_HTTP_LISTENER_PORT;
    }
}
