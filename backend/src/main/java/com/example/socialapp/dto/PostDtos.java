package com.example.socialapp.dto;

import java.time.Instant;
import java.util.List;

public class PostDtos {

    public record LocationDto(
            Double latitude,
            Double longitude,
            String address
    ) {}

    public record PhotoResponse(
            Long id,
            String url,
            String originalFilename,
            int order
    ) {}

    public record PostResponse(
            Long id,
            Long authorId,
            String authorUsername,
            String caption,
            Instant createdAt,
            LocationDto location,
            List<PhotoResponse> photos
    ) {}
}
