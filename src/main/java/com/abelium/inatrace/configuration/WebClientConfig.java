package com.abelium.inatrace.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * HU-10: Configured WebClient beans with connection, read, and response timeouts
 * using Reactor Netty HttpClient to prevent thread hanging on external HTTP calls.
 */
@Configuration
public class WebClientConfig {

    public static final int CONNECT_TIMEOUT_MILLIS = 5000;
    public static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public HttpClient nettyHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(RESPONSE_TIMEOUT)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS)));
    }

    @Bean
    public WebClient.Builder webClientBuilder(HttpClient nettyHttpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(nettyHttpClient));
    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
