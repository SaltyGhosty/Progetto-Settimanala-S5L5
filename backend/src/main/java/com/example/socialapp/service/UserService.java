package com.example.socialapp.service;

import com.example.socialapp.dto.UserDtos.UserProfileResponse;
import com.example.socialapp.entity.User;
import com.example.socialapp.repository.DocumentRepository;
import com.example.socialapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PostRepository postRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse profileOf(User user) {
        long postCount = postRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged()).getTotalElements();
        long documentCount = documentRepository.findByOwnerIdOrderByUploadedAtDesc(user.getId()).size();

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                postCount,
                documentCount
        );
    }
}
