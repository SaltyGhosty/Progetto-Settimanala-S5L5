package com.example.socialapp.dto;

public class GeocodeDtos {

    public record GeocodeResult(
            Double latitude,
            Double longitude,
            String displayName
    ) {}
}
