package com.example.javaBackend.controller.dto;

public record LoginResponse(String accessToken,Long expireIn) {
}
