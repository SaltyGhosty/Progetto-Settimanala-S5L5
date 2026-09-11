package com.example.socialapp.service;

import com.example.socialapp.dto.GeocodeDtos.GeocodeResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;

/**
 * Client per la Geocoding API v4 di Google (geocode.googleapis.com/v4/geocode/...).
 * Uso la v4 e non l'endpoint "legacy" (maps.googleapis.com/maps/api/geocode) perché
 * solo la v4 è coperta dalla Maps Demo Key gratuita usata in questo progetto.
 */
@Service
@Slf4j
public class GeocodingService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://geocode.googleapis.com/v4/geocode")
            .build();

    @Value("${app.google.maps-api-key}")
    private String apiKey;

    public GeocodeResult forward(String address) {
        try {
            JsonNode body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/address/{address}")
                            .build(address))
                    .header("X-Goog-Api-Key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            return firstResult(body);
        } catch (Exception e) {
            log.warn("Forward geocoding failed for address '{}': {}", address, e.getMessage());
            return null;
        }
    }

    public GeocodeResult reverse(double latitude, double longitude) {
        try {
            String latLng = String.format(Locale.ROOT, "%.6f,%.6f", latitude, longitude);
            JsonNode body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/location/{latLng}")
                            .build(latLng))
                    .header("X-Goog-Api-Key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            return firstResult(body);
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for ({}, {}): {}", latitude, longitude, e.getMessage());
            return null;
        }
    }

    private GeocodeResult firstResult(JsonNode body) {
        if (body == null) {
            return null;
        }
        JsonNode results = body.get("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            return null;
        }
        JsonNode first = results.get(0);
        JsonNode location = first.path("location");
        return new GeocodeResult(
                location.get("latitude").asDouble(),
                location.get("longitude").asDouble(),
                first.get("formattedAddress").asText()
        );
    }
}
