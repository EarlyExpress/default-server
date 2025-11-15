package com.early_express.default_server.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${webclient.timeout.connection:5000}")
    private int connectionTimeout;

    @Value("${webclient.timeout.read:10000}")
    private int readTimeout;

    @Value("${webclient.timeout.write:10000}")
    private int writeTimeout;

    @Value("${webclient.max-memory-size:10485760}") // 10MB
    private int maxMemorySize;

    /**
     * 기본 WebClient - 외부 API 호출용
     */
    @Bean
    public WebClient webClient() {
        return createWebClient()
                .baseUrl("")
                .build();
    }

    /**
     * LoadBalanced WebClient - 내부 MSA 서비스 호출용
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return createWebClient();
    }

    /**
     * WebClient Builder 생성
     */
    private WebClient.Builder createWebClient() {
        // Connection Provider 설정
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // HTTP Client 설정
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
                );

        // Exchange Strategies 설정 (메모리 제한)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(maxMemorySize);
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleError());
    }

    /**
     * Request 로깅 필터
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> log.debug("Request Header: {}={}", name, value))
            );
            return Mono.just(clientRequest);
        });
    }

    /**
     * Response 로깅 필터
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response Status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    values.forEach(value -> log.debug("Response Header: {}={}", name, value))
            );
            return Mono.just(clientResponse);
        });
    }

    /**
     * 에러 처리 필터
     */
    private ExchangeFilterFunction handleError() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                log.error("Server error: {}", clientResponse.statusCode());
            } else if (clientResponse.statusCode().is4xxClientError()) {
                log.warn("Client error: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}