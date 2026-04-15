package com.example.geolocation.application.domain.model;

public record Region(
    String code,
    String name
) {
    public Region {
        if (code == null) code = "";
        if (name == null) name = "";
    }
}

