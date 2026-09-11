package com.example.socialapp.controller;

import com.example.socialapp.dto.UserDtos.UserProfileResponse;
import com.example.socialapp.security.SocialUserDetails;
import com.example.socialapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal SocialUserDetails principal) {
        return userService.profileOf(principal.getDomainUser());
    }
}
