package com.example.javaBackend.service;

import com.example.javaBackend.controller.dto.UserWithPersonDto;
import com.example.javaBackend.entity.Person;
import com.example.javaBackend.entity.Role;
import com.example.javaBackend.entity.User;
import com.example.javaBackend.repository.RoleRepository;
import com.example.javaBackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(
            UserRepository userRepository
            , RoleRepository roleRepository
            , BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ResponseEntity<?> createUser(UserWithPersonDto createUserDto) {

        Role basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        Optional<User> userDb = userRepository.findUserByName(createUserDto.name());
        if (userDb.isPresent()) {
            logger.warn("Foi feito tentativa de criar usuário com nome já em uso. Usuário = {}", createUserDto.name());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        User newUser = new User();
        newUser.setName(createUserDto.name());
        newUser.setEmail(createUserDto.email());
        newUser.setPassword(passwordEncoder.encode(createUserDto.password()));
        newUser.setRoles(Set.of(basicRole));

        Person newPerson = new Person();
        newPerson.setName(createUserDto.person().name());
        newPerson.setCpfNumber(createUserDto.person().cpf());
        newPerson.setPhoneNumber(createUserDto.person().phone());
        newPerson.setUser(newUser);
        newUser.setPerson(newPerson);

        userRepository.save(newUser);
        logger.info("Novo usuário criado: {}", newUser.getName());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<List<User>> listAll() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    public ResponseEntity<List<User>> listUsers(String name, String email) {

        boolean isNameBlank = name.isBlank();
        boolean isEmailBlank = email.isBlank();
        List<User> users;

        if (isNameBlank && isEmailBlank) {
            users = userRepository.findAll();
            return ResponseEntity.ok(users);
        }

        if (!isNameBlank) {
            if (!isEmailBlank) {
                users = userRepository.findByNameAndEmail(name, email);
                return ResponseEntity.ok(users);
            } else {
                users = userRepository.findByName(name);
                return ResponseEntity.ok(users);
            }
        }

        users = userRepository.findByEmail(email);
        return ResponseEntity.ok(users);

    }

    @Transactional
    public ResponseEntity<User> updateUser(UserWithPersonDto userUpdateDto, JwtAuthenticationToken token) {
        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));
        if (tokenUser.isEmpty()) {
            logger.warn("Acesso com token contendo UUID inválido durante PATCH. Token = {}", token);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<User> userDb = userRepository.findUserByName(userUpdateDto.name());
        if (userDb.isEmpty()) {
            logger.warn("Atualização em usuário inexistente. Usuário = {}", userUpdateDto.name());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (tokenUser.get().getId().equals(userDb.get().getId())) {
            userDb.get().setName(userUpdateDto.name());
            userDb.get().setEmail(userUpdateDto.email());
            userDb.get().setPassword(passwordEncoder.encode(userUpdateDto.password()));
            userRepository.save(userDb.get());
            logger.debug("Atualização de usuário realizada. Usuário = {} , UUID = {}"
                    , userUpdateDto.name(), tokenUser.get().getId());
            return ResponseEntity.ok().build();
        }

        boolean isAdmin = tokenUser.get().getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));
        if (isAdmin) {
            userDb.get().setName(userUpdateDto.name());
            userDb.get().setEmail(userUpdateDto.email());
            userDb.get().setPassword(passwordEncoder.encode(userUpdateDto.password()));
            userRepository.save(userDb.get());
            logger.debug("Atualização de usuário realizada como ADMIN. Usuário = {} , UUID = {}"
                    , userUpdateDto.name(), tokenUser.get().getId());
            return ResponseEntity.ok().build();
        } else return ResponseEntity.status(HttpStatus.FORBIDDEN).build();


    }

    @Transactional
    public ResponseEntity<Void> deleteUser(String name, JwtAuthenticationToken token) {

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));

        if (tokenUser.isEmpty()) {
            logger.warn("Acesso com token contendo UUID inválido durante DELETE. Token = {}", token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        boolean isAdmin = tokenUser.get().getRoles()
                .stream().anyMatch(role -> role.getId().equals(Role.Values.ADMIN.getId()));
        if (!isAdmin) {
            logger.warn("Operação de DELETE sem role 'admin'. Usuário = {}", tokenUser.get().getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<User> userDelete = userRepository.findUserByName(name);
        if (userDelete.isPresent()) {
            userRepository.deleteById(userDelete.get().getId());
            logger.info("Operação de DELETE em usuário realizada. Usuário deletado = {}"
                    ,name);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Operação de DELETE em usuário inexistente. Usuário a ser deletado = {}"
                    ,name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<User> getTokenUserInfo(JwtAuthenticationToken token) {

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));

        if(tokenUser.isPresent()){
            return ResponseEntity.ok().body(tokenUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }
}
