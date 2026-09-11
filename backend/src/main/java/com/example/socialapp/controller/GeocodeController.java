package com.example.socialapp.controller;

import com.example.socialapp.dto.GeocodeDtos.GeocodeResult;
import com.example.socialapp.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Fa da tramite verso Google: converte un indirizzo in coordinate e viceversa. */
@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
public class GeocodeController {

    private final GeocodingService geocodingService;

    @GetMapping("/search")
    public ResponseEntity<GeocodeResult> search(@RequestParam String address) {
        GeocodeResult result = geocodingService.forward(address);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/reverse")
    public ResponseEntity<GeocodeResult> reverse(@RequestParam double lat, @RequestParam double lon) {
        GeocodeResult result = geocodingService.reverse(lat, lon);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }
}
