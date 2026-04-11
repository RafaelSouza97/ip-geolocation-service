package com.example.geolocation.application.domain.model;

/**
 * Representa uma região/estado com código e nome.
 *
 * @param code código da região (ex: "SP", "CA")
 * @param name nome completo da região (ex: "São Paulo", "California")
 */
public record Region(
    String code,
    String name
) {
    public Region {
        // Region pode ser nula para alguns IPs
        if (code == null) code = "";
        if (name == null) name = "";
    }
}
