package com.amazonaws.sagemaker.configuration;

import com.amazonaws.sagemaker.utils.CommonUtils;
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
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DependencyConfiguration {

    private static final String DEFAULT_HTTP_LISTENER_PORT = "8080";
    private static final Integer MAX_CORE_TO_THREAD_RATIO = 10;
    private static final Integer MIN_CORE_TO_THREAD_RATIO = 2;
    private static final String MODEL_LOCATION = "/opt/ml/model";

    @Bean
    @Scope(value = "application")
    public String provideModelLocation() {
        return MODEL_LOCATION;
    }

    @Bean
    @Scope(value = "application")
    public ContextBuilder provideContextBuilder() {
        return new ContextBuilder();
    }

    @Bean
    @Scope(value = "application")
    public MleapContext provideMleapContext(ContextBuilder contextBuilder) {
        return contextBuilder.createMleapContext();
    }

    @Bean
    @Scope(value = "application")
    public BundleBuilder provideBundleBuilder() {
        return new BundleBuilder();
    }

    @Bean
    @Scope(value = "application")
    public LeapFrameBuilder provideLeapFrameBuilder() {
        return new LeapFrameBuilder();
    }

    @Bean
    @Scope(value = "application")
    public LeapFrameBuilderSupport provideLeapFrameBuilderSupport() {
        return new LeapFrameBuilderSupport();
    }

    @Bean
    @Scope(value = "application")
    public Transformer provideTransformer(String modelLocation, BundleBuilder bundleBuilder,
        MleapContext mleapContext) {
        return bundleBuilder.load(new File(modelLocation), mleapContext).root();
    }

    @Bean
    public JettyServletWebServerFactory provideJettyServletWebServerFactory() {
        final String listenerPort =
            (System.getenv("SAGEMAKER_BIND_TO_PORT") != null) ? System.getenv("SAGEMAKER_BIND_TO_PORT")
                : DEFAULT_HTTP_LISTENER_PORT;
        final JettyServletWebServerFactory jettyServlet = new JettyServletWebServerFactory(new Integer(listenerPort));
        final List<JettyServerCustomizer> serverCustomizerList = Lists.newArrayList();
        final JettyServerCustomizer serverCustomizer = server -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMinThreads(CommonUtils.getNumberOfThreads(MIN_CORE_TO_THREAD_RATIO));
            threadPool.setMaxThreads(CommonUtils.getNumberOfThreads(MAX_CORE_TO_THREAD_RATIO));
        };
        serverCustomizerList.add(serverCustomizer);
        jettyServlet.setServerCustomizers(serverCustomizerList);
        return jettyServlet;
    }
}
