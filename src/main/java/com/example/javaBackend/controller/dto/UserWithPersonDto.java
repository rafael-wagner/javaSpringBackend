package com.example.javaBackend.controller.dto;

public record UserWithPersonDto(String name, String password, String email, PersonDto person){
}
