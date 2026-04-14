package com.example.geolocation.application.domain.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum que representa as plataformas de dispositivo suportadas.
 */
public enum Platform {
    
    IOS("iOS"),
    ANDROID("Android"),
    WEB("Web");

    private final String value;

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
     * Retorna um conjunto com todos os valores válidos de plataforma.
     */
    public static Set<String> validValues() {
        return Arrays.stream(values())
                .map(Platform::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * Retorna uma string formatada com os valores válidos.
     */
    public static String validValuesAsString() {
        return Arrays.stream(values())
                .map(Platform::getValue)
                .collect(Collectors.joining(", "));
    }

    /**
     * Verifica se um valor é uma plataforma válida.
     */
    public static boolean isValid(String value) {
        return validValues().contains(value);
    }

    /**
     * Converte uma string para o enum Platform.
     * @throws IllegalArgumentException se o valor não for válido
     */
    public static Platform fromValue(String value) {
        return Arrays.stream(values())
                .filter(p -> p.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid platform: " + value));
    }
}
