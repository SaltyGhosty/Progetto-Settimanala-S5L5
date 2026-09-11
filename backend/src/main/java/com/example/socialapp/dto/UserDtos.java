package com.example.socialapp.dto;

import java.time.Instant;

public class UserDtos {

    public record UserProfileResponse(
            Long id,
            String username,
            String email,
            Instant createdAt,
            long postCount,
            long documentCount
    ) {}
}
