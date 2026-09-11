package com.example.socialapp.dto;

import java.time.Instant;

public class DocumentDtos {

    public record DocumentResponse(
            Long id,
            String originalFilename,
            String contentType,
            long sizeBytes,
            String status,
            String ocrText,
            String ocrError,
            Instant uploadedAt,
            Instant processedAt
    ) {}
}
