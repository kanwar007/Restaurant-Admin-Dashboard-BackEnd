package com.cafeadmin.identity.api.dto;

import com.cafeadmin.identity.domain.StaffUser;

public record UserResponse(String id, String username, String name, String role, String initials) {

    public static UserResponse of(StaffUser user) {
        return new UserResponse(user.getId().toString(), user.getUsername(), user.getName(), user.getRole(),
                user.getInitials());
    }
}
