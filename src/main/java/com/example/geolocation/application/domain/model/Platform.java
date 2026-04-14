package com.example.geolocation.application.domain.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum que representa as plataformas de dispositivo suportadas.
 */
public enum Platform {

    IOS("iOS"), ANDROID("Android"), WEB("Web");

    private final String value;

    // Cache dos valores válidos (calculado uma única vez)
    private static final Set<String> VALID_VALUES_SET = Collections.unmodifiableSet(
        Arrays.stream(values()).map(Platform::getValue).collect(Collectors.toSet())
    );
    
    private static final String VALID_VALUES_STRING = 
        Arrays.stream(values()).map(Platform::getValue).collect(Collectors.joining(", "));

    Platform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Retorna um conjunto imutável com todos os valores válidos de plataforma.
     */
    public static Set<String> validValues() {
        return VALID_VALUES_SET;
    }

    /**
     * Retorna uma string formatada com os valores válidos.
     */
    public static String validValuesAsString() {
        return VALID_VALUES_STRING;
    }

    /**
     * Verifica se um valor é uma plataforma válida.
     */
    public static boolean isValid(String value) {
        return VALID_VALUES_SET.contains(value);
    }

    /**
     * Converte uma string para o enum Platform.
     * 
     * @throws IllegalArgumentException se o valor não for válido
     */
    public static Platform fromValue(String value) {
        return Arrays.stream(values()).filter(p -> p.getValue().equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        com.example.geolocation.application.domain.exception.ErrorCode.INVALID_PLATFORM_SIMPLE
                                .format(value)));
    }
}
