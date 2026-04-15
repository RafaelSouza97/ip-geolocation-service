package com.example.geolocation.application.domain.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Platform {

    IOS("iOS"), ANDROID("Android"), WEB("Web");

    private final String value;
    private static final Set<String> VALID_VALUES_SET = Collections.unmodifiableSet(
            Arrays.stream(values()).map(Platform::getValue).collect(Collectors.toSet()));

    private static final String VALID_VALUES_STRING =
            Arrays.stream(values()).map(Platform::getValue).collect(Collectors.joining(", "));

    @Override
    public String toString() {
        return value;
    }

    
    public static Set<String> validValues() {
        return VALID_VALUES_SET;
    }

    
    public static String validValuesAsString() {
        return VALID_VALUES_STRING;
    }

    
    public static boolean isValid(String value) {
        return VALID_VALUES_SET.contains(value);
    }

    
    public static Platform fromValue(String value) {
        return Arrays.stream(values()).filter(p -> p.getValue().equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        com.example.geolocation.application.domain.exception.ErrorCode.INVALID_PLATFORM_SIMPLE
                                .format(value)));
    }
}


