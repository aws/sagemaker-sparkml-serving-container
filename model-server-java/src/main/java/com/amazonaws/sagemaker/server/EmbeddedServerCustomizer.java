package com.amazonaws.sagemaker.server;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class EmbeddedServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    private static final String DEFAULT_HTTP_LISTENER_PORT = "8080";

    @Override
    public void customize(ConfigurableWebServerFactory webServerFactory) {
        String listenerPort = (System.getenv("SAGEMAKER_BIND_TO_PORT") != null) ? System.getenv(
                "SAGEMAKER_BIND_TO_PORT") : DEFAULT_HTTP_LISTENER_PORT;
        webServerFactory.setPort(new Integer(listenerPort));
    }
}