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
 * Configuration for WebClient used to make HTTP requests to Git APIs
 */
@Configuration
public class WebClientConfig {

    /**
     * Default timeout in milliseconds
     */
    private static final int TIMEOUT = 10000;
    
    /**
     * Memory limit for response data in bytes (10MB)
     */
    private static final int MEMORY_LIMIT = 10 * 1024 * 1024;

    /**
     * Create a WebClient builder with common configuration
     * @return configured WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configure timeout and connection options
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .responseTimeout(Duration.ofMillis(TIMEOUT))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)));
                        
        // Configure memory limits for response size
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(MEMORY_LIMIT))
                .build();
                
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);
    }
    
    /**
     * Create a GitHub API WebClient
     * @param webClientBuilder the base WebClient.Builder
     * @return WebClient configured for GitHub API
     */
    @Bean
    public WebClient githubWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }
    
    /**
     * Create a GitLab API WebClient
     * @param webClientBuilder the base WebClient.Builder
     * @return WebClient configured for GitLab API
     */
    @Bean
    public WebClient gitlabWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("https://gitlab.com/api/v4")
                .build();
    }
}