package com.example.geolocation.infrastructure.adapter.out.client;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.example.geolocation.application.domain.exception.ErrorCode;
import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * Seletor de provedor com lógica de circuit breaker.
 * 
 * Comportamento: 1. Tenta o provedor primário (ip-api.com) 2. Se falhar, muda para o secundário
 * (ipapi.co) por 5 minutos 3. Durante o período de failover, todas as requisições vão para o
 * secundário 4. Se o secundário também falhar, tenta o primário 5. Se ambos falharem, lança exceção
 * (fallback local será tratado pelo service)
 * 
 * Após o período de failover, tenta novamente o provedor original.
 */
@Slf4j
@Primary
@Component
public class ProviderSelector implements GeolocationProvider {

    private final GeolocationProvider primaryProvider;
    private final GeolocationProvider secondaryProvider;
    private final Duration failoverDuration;

    // Estado do circuit breaker
    private final AtomicReference<ProviderState> currentState =
            new AtomicReference<>(new ProviderState(ProviderType.PRIMARY, null));

    public ProviderSelector(@Qualifier("ipApiClient") GeolocationProvider primaryProvider,
            @Qualifier("ipApiCoClient") GeolocationProvider secondaryProvider,
            GeolocationProperties properties) {
        this.primaryProvider = primaryProvider;
        this.secondaryProvider = secondaryProvider;
        this.failoverDuration = properties.providers().failoverDuration();

        log.info("ProviderSelector initialized with failover duration: {}", failoverDuration);
    }

    @Override
    public GeolocationInfo lookup(String ip) {
        ProviderState state = currentState.get();

        // Verificar se o período de failover expirou
        if (state.isInFailover() && state.hasFailoverExpired(failoverDuration)) {
            log.info("Failover period expired, resetting to primary provider");
            currentState.compareAndSet(state, new ProviderState(ProviderType.PRIMARY, null));
            state = currentState.get();
        }

        // Determinar qual provedor usar com base no estado atual
        if (state.activeProvider() == ProviderType.PRIMARY) {
            return tryWithFailover(ip, primaryProvider, secondaryProvider, ProviderType.SECONDARY);
        } else {
            return tryWithFailover(ip, secondaryProvider, primaryProvider, ProviderType.PRIMARY);
        }
    }

    private GeolocationInfo tryWithFailover(String ip, GeolocationProvider activeProvider,
            GeolocationProvider fallbackProvider, ProviderType fallbackType) {

        try {
            return activeProvider.lookup(ip);
        } catch (ExternalApiException e) {
            log.warn("Provider failed, attempting failover: {}", e.getMessage());

            try {
                GeolocationInfo result = fallbackProvider.lookup(ip);

                // Sucesso no fallback - ativar período de failover
                ProviderState newState = new ProviderState(fallbackType, Instant.now());
                currentState.set(newState);
                log.info("Switched to {} provider for {} minutes", fallbackType,
                        failoverDuration.toMinutes());

                return result;
            } catch (ExternalApiException e2) {
                log.error("Both providers failed. Primary: {}, Secondary: {}", e.getMessage(),
                        e2.getMessage());
                // Ambos falharam - lançar exceção para fallback local
                throw new ExternalApiException("all-providers",
                        ErrorCode.BOTH_PROVIDERS_FAILED.format(e.getMessage(), e2.getMessage()));
            }
        }
    }

    /**
     * Retorna o nome do provedor atualmente ativo.
     */
    public String getActiveProviderName() {
        return currentState.get().activeProvider().name();
    }

    /**
     * Verifica se está em modo failover.
     */
    public boolean isInFailover() {
        ProviderState state = currentState.get();
        return state.isInFailover() && !state.hasFailoverExpired(failoverDuration);
    }

    /**
     * Reseta o estado para o provedor primário (útil para testes).
     */
    public void reset() {
        currentState.set(new ProviderState(ProviderType.PRIMARY, null));
        log.info("ProviderSelector reset to primary provider");
    }

    enum ProviderType {
        PRIMARY, SECONDARY
    }

    record ProviderState(ProviderType activeProvider, Instant failoverStartTime) {
        boolean isInFailover() {
            return failoverStartTime != null;
        }

        boolean hasFailoverExpired(Duration failoverDuration) {
            if (failoverStartTime == null)
                return true;
            return Instant.now().isAfter(failoverStartTime.plus(failoverDuration));
        }
    }
}
