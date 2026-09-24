package com.abelium.inatrace.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class WebClientConfigTest {

    @Autowired
    private HttpClient nettyHttpClient;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private WebClient webClient;

    @Test
    void testWebClientBeansAreConfigured() {
        assertNotNull(nettyHttpClient, "Reactor Netty HttpClient bean should be present");
        assertNotNull(webClientBuilder, "WebClient.Builder bean should be present");
        assertNotNull(webClient, "WebClient bean should be present");
    }
}
