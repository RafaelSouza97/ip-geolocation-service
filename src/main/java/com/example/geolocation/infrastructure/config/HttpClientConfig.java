package com.example.geolocation.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuração do HttpClient compartilhado entre os clientes de API externa.
 */
@Configuration
public class HttpClientConfig {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Cria um HttpClient compartilhado para otimizar conexões.
     * 
     * @param properties propriedades de geolocalização
     * @return HttpClient configurado
     */
    @Bean
    public HttpClient sharedHttpClient(GeolocationProperties properties) {
        Duration timeout = properties.api() != null && properties.api().timeout() != null 
            ? properties.api().timeout() 
            : DEFAULT_CONNECT_TIMEOUT;
        
        return HttpClient.newBuilder()
            .connectTimeout(timeout)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }
}
