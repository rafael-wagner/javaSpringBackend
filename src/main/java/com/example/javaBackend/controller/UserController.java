package com.example.javaBackend.controller;

import com.example.javaBackend.entity.User;
import com.example.javaBackend.entity.jsonview.PersonView;
import com.example.javaBackend.entity.jsonview.UserView;
import com.example.javaBackend.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public ResponseEntity<String[]> firstPage() {
        return ResponseEntity.ok(new String[]{"HELLO"});
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody
                                            @JsonView(PersonView.SelfView.class) User newUser) {
        return userService.createUser(newUser);
    }

    @GetMapping("admin/findAll")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @JsonView(PersonView.Admin.class)
    public ResponseEntity<List<User>> listAllUsers() {

        return userService.listAll();

    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    @JsonView(PersonView.Basic.class)
    public ResponseEntity<List<User>> searchUserByUserNameAndEmail(
            @RequestParam(defaultValue = "") String name
            , @RequestParam(defaultValue = "") String email ) {

        return userService.listUsers(name,email);

    }

    @GetMapping("/info")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    @JsonView(PersonView.SelfView.class)
    public ResponseEntity<User> getTokenUserInfo(JwtAuthenticationToken token){
        return userService.getTokenUserInfo(token);
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> updateUser(
            @RequestBody @JsonView(PersonView.SelfView.class) User userUpdate
            , JwtAuthenticationToken token) {

        return userService.updateUser(userUpdate,token);
    }

    @PutMapping("/admin")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> updateUserAsAdmin(
            @RequestBody @JsonView(PersonView.Admin.class) User userUpdateDto
            , JwtAuthenticationToken token) {

        return userService.updateUserAsAdmin(userUpdateDto,token);
    }

    @DeleteMapping("/admin")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> deleteUser(
            @RequestParam String name
            , JwtAuthenticationToken token) {

        return userService.deleteUser(name,token);

    }

}
