package com.campingtripplanner.backend.web.dto;

import com.campingtripplanner.backend.domain.User;

public record RegisterResponse(Long id, String username) {

    public static RegisterResponse from(User user) {
        return new RegisterResponse(user.getId(), user.getUsername());
    }
}
