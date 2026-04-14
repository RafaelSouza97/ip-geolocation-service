package com.example.geolocation.application.service;

import java.util.regex.Pattern;

/**
 * Validador de endereços IP (IPv4 e IPv6).
 * Detecta IPs inválidos, privados e localhost.
 */
public final class IpValidator {

    private IpValidator() {
        // Utility class
    }

    // IPv4: Cada octeto de 0-255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.){3}(25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)$"
    );

    // IPv6: Formato completo e comprimido
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +                    // Full
        "([0-9a-fA-F]{1,4}:){1,7}:|" +                                   // Ends with ::
        "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +                   // :: in middle
        "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
        "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
        "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
        "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
        "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
        ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +                               // Starts with ::
        "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|" +                  // Link-local
        "::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?\\d)?\\d)\\.){3}" +     // IPv4-mapped
        "(25[0-5]|(2[0-4]|1?\\d)?\\d)|" +
        "([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?\\d)?\\d)\\.){3}" +
        "(25[0-5]|(2[0-4]|1?\\d)?\\d))$"
    );

    // IPs Privados IPv4
    private static final Pattern PRIVATE_IPV4_PATTERN = Pattern.compile(
        "^(10\\." +                                    // 10.0.0.0/8
        "|172\\.(1[6-9]|2\\d|3[01])\\." +              // 172.16.0.0/12
        "|192\\.168\\." +                              // 192.168.0.0/16
        "|127\\." +                                    // 127.0.0.0/8 (localhost)
        "|0\\." +                                      // 0.0.0.0/8
        "|169\\.254\\." +                              // 169.254.0.0/16 (link-local)
        "|100\\.(6[4-9]|[7-9]\\d|1[01]\\d|12[0-7])\\." + // 100.64.0.0/10 (CGNAT)
        ")"
    );

    // Localhost IPv6
    private static final Pattern LOCALHOST_IPV6_PATTERN = Pattern.compile(
        "^(::1|0:0:0:0:0:0:0:1)$"
    );

    // Link-local IPv6
    private static final Pattern PRIVATE_IPV6_PATTERN = Pattern.compile(
        "^(fe80:|fc00:|fd00:)", Pattern.CASE_INSENSITIVE
    );

    /**
     * Verifica se o IP é válido (IPv4 ou IPv6).
     *
     * @param ip endereço IP
     * @return true se válido
     */
    public static boolean isValid(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();
        return isValidIpv4(trimmed) || isValidIpv6(trimmed);
    }

    /**
     * Verifica se é um IPv4 válido.
     */
    public static boolean isValidIpv4(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * Verifica se é um IPv6 válido.
     */
    public static boolean isValidIpv6(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return IPV6_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * Verifica se é um IP privado ou reservado.
     * IPs privados incluem: localhost, link-local, redes privadas (RFC 1918).
     *
     * @param ip endereço IP
     * @return true se privado/reservado
     */
    public static boolean isPrivateOrReserved(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();
        
        // IPv4 privados
        if (isValidIpv4(trimmed)) {
            return PRIVATE_IPV4_PATTERN.matcher(trimmed).find();
        }
        
        // IPv6 privados/localhost
        if (isValidIpv6(trimmed)) {
            return LOCALHOST_IPV6_PATTERN.matcher(trimmed).matches() ||
                   PRIVATE_IPV6_PATTERN.matcher(trimmed).find();
        }
        
        return false;
    }

    /**
     * Verifica se é localhost (127.x.x.x ou ::1).
     */
    public static boolean isLocalhost(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();
        return trimmed.startsWith("127.") || 
               LOCALHOST_IPV6_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Verifica se é um IP público válido (não privado, não localhost).
     *
     * @param ip endereço IP
     * @return true se público e válido
     */
    public static boolean isPublic(String ip) {
        return isValid(ip) && !isPrivateOrReserved(ip);
    }

    /**
     * Normaliza o IP (trim e lowercase para IPv6).
     *
     * @param ip endereço IP a ser normalizado
     * @return IP normalizado
     * @throws IllegalArgumentException se ip for null ou vazio
     */
    public static String normalize(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("IP cannot be null or blank");
        }
        String trimmed = ip.trim();
        if (isValidIpv6(trimmed)) {
            return trimmed.toLowerCase();
        }
        return trimmed;
    }
}
