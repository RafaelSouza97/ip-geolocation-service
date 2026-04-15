package com.example.geolocation.infrastructure.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class HttpClientConfig {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);

    
    @Bean
    public HttpClient sharedHttpClient(GeolocationProperties properties) {
        Duration timeout = properties.api() != null && properties.api().timeout() != null
                ? properties.api().timeout()
                : DEFAULT_CONNECT_TIMEOUT;

        log.info("Configuring shared HttpClient with timeout: {}", timeout);

        return HttpClient.newBuilder().connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL).build();
    }
}

