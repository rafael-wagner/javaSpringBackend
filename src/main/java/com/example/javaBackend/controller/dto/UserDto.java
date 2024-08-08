package com.example.javaBackend.controller.dto;

import java.util.UUID;

public record UserDto(UUID id, String name, String password) {
}
