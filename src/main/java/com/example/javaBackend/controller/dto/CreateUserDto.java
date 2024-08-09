package com.example.javaBackend.controller.dto;

public record CreateUserDto (String name, String password, String email, PersonDto person){
}
