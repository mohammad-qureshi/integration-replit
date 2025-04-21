package com.gitintegration.api.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a WebClient bean with custom configuration.
     * 
     * @return Configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofMillis(10000))
                .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(10000, TimeUnit.MILLISECONDS)));
        
        // Increase the max in-memory size for larger responses
        final int size = 16 * 1024 * 1024; // 16MB
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
