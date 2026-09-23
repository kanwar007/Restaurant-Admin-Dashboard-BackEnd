package com.cafeadmin.identity.api;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.cafeadmin.identity.api.dto.UserResponse;
import com.cafeadmin.identity.service.AuthService;
import com.cafeadmin.identity.service.ProfileService;

@RestController
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    public ProfileController(ProfileService profileService, AuthService authService) {
        this.profileService = profileService;
        this.authService = authService;
    }

    @GetMapping("/api/profile")
    public Map<String, Object> profile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String header) {
        UserResponse user = authService.currentUser(BearerToken.from(header))
                .map(UserResponse::of)
                .orElseGet(profileService::defaultUser);
        return Map.of("restaurant", profileService.restaurant(), "user", user);
    }

    @GetMapping("/internal/restaurant")
    public Object restaurant() {
        return profileService.restaurant();
    }
}
