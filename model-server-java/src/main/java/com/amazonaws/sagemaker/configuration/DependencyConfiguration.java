package com.amazonaws.sagemaker.configuration;

import ml.combust.mleap.runtime.MleapContext;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.javadsl.BundleBuilder;
import ml.combust.mleap.runtime.javadsl.ContextBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilderSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.nio.charset.Charset;

@Configuration
public class DependencyConfiguration {

    @Bean
    @Scope(value = "application")
    public String provideModelLocation() {
        return "/opt/ml/model";
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
    @Scope (value = "application")
    public Charset provideDefaultCharset() {
        return Charset.forName("UTF-8");
    }

}
