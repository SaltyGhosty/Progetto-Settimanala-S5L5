package com.example.socialapp.controller;

import com.example.socialapp.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serve le foto dei post. Endpoint pubblico: vedi SecurityConfig per il motivo. */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @GetMapping("/photos/{filename}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String filename) {
        Resource resource = storageService.loadAsResource("photos", filename);
        MediaType mediaType = filename.endsWith(".png") ? MediaType.IMAGE_PNG
                : filename.endsWith(".webp") ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
