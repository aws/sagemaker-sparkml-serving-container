package com.amazonaws.sagemaker.server;

import com.amazonaws.sagemaker.utils.CommonUtils;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class EmbeddedJettyCustomizer implements WebServerFactoryCustomizer<ConfigurableJettyWebServerFactory> {

    private static final String DEFAULT_HTTP_LISTENER_PORT = "8080";


    @Override
    public void customize(final ConfigurableJettyWebServerFactory factory) {
        String listenerPort =
            (System.getenv("SAGEMAKER_BIND_TO_PORT") != null) ? System.getenv("SAGEMAKER_BIND_TO_PORT")
                : DEFAULT_HTTP_LISTENER_PORT;
        factory.setPort(new Integer(listenerPort));
        factory.addServerCustomizers((JettyServerCustomizer) server -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMaxThreads(CommonUtils.getNumberOfThreads());
        });
    }
}