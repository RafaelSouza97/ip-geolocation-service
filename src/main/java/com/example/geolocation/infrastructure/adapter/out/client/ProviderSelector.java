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

@Slf4j
@Primary
@Component
public class ProviderSelector implements GeolocationProvider {

    private final GeolocationProvider primaryProvider;
    private final GeolocationProvider secondaryProvider;
    private final Duration failoverDuration;
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
        if (state.isInFailover() && state.hasFailoverExpired(failoverDuration)) {
            log.info("Failover period expired, resetting to primary provider");
            currentState.compareAndSet(state, new ProviderState(ProviderType.PRIMARY, null));
            state = currentState.get();
        }
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
                ProviderState newState = new ProviderState(fallbackType, Instant.now());
                currentState.set(newState);
                log.info("Switched to {} provider for {} minutes", fallbackType,
                        failoverDuration.toMinutes());

                return result;
            } catch (ExternalApiException e2) {
                log.error("Both providers failed. Primary: {}, Secondary: {}", e.getMessage(),
                        e2.getMessage());
                throw new ExternalApiException("all-providers",
                        ErrorCode.BOTH_PROVIDERS_FAILED.format(e.getMessage(), e2.getMessage()));
            }
        }
    }

    public String getActiveProviderName() {
        return currentState.get().activeProvider().name();
    }

    public boolean isInFailover() {
        ProviderState state = currentState.get();
        return state.isInFailover() && !state.hasFailoverExpired(failoverDuration);
    }

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
            if (failoverStartTime == null) {
                return true;
            }
            return Instant.now().isAfter(failoverStartTime.plus(failoverDuration));
        }
    }
}

