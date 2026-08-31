package com.designtouch.backend.dto;

public record LoginResponse(String token, String username, String role) {
}
