package com.example.javaBackend.controller;

import com.example.javaBackend.controller.dto.UserWithPersonDto;
import com.example.javaBackend.entity.User;
import com.example.javaBackend.entity.jsonview.View;
import com.example.javaBackend.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody UserWithPersonDto createUserDto) {
        return userService.createUser(createUserDto);
    }

    @GetMapping("/hello")
    public ResponseEntity<String[]> firstPage() {
        return ResponseEntity.ok(new String[]{"HELLO"});
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @JsonView(View.Admin.class)
    public ResponseEntity<List<User>> listAllUsers() {

        return userService.listAll();

    }

    @GetMapping()
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    @JsonView(View.Basic.class)
    public ResponseEntity<List<User>> searchUserByUserNameAndEmail(
            @RequestParam(defaultValue = "") String name
            , @RequestParam(defaultValue = "") String email ) {

        return userService.listUsers(name,email);

    }


    @PutMapping()
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<User> updateUser(
            @RequestBody UserWithPersonDto userUpdateDto
            , JwtAuthenticationToken token) {

        return userService.updateUser(userUpdateDto,token);

    }

    @Transactional
    @DeleteMapping()
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @RequestParam String name
            , JwtAuthenticationToken token) {

        return userService.deleteUser(name,token);

    }

}
