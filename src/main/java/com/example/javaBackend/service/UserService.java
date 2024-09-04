package com.example.javaBackend.service;

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
    public ResponseEntity<?> createUser(User newUser) {

        if (!isUserFieldsValid(newUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Role basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        Optional<User> userDb = userRepository.findUserByName(newUser.getName());
        if (userDb.isPresent()) {
            logger.warn("Foi feito tentativa de criar usuário com nome já em uso. Usuário = {}", newUser.getName());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        newUser.setName(newUser.getName().toLowerCase());
        newUser.setEmail(newUser.getEmail().toLowerCase());
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setRoles(Set.of(basicRole));

        Person newPerson = new Person();
        newPerson.setName(newUser.getPerson().getName().toLowerCase());
        newPerson.setCpfNumber(newUser.getPerson().getCpfNumber());
        newPerson.setPhoneNumber(newUser.getPerson().getPhoneNumber());
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
    public ResponseEntity<?> updateUser(User userUpdate, JwtAuthenticationToken token) {

        if (!isUserAnyFieldsValid(userUpdate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));
        if (tokenUser.isEmpty()) {
            logger.warn("Acesso com token contendo UUID inválido durante PUT. Token = {}", token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<User> userDb = userRepository.findUserByName(userUpdate.getName());
        if (userDb.isPresent()) {
            logger.warn("Atualização com nome de usuário já existente. Usuário = {}", userUpdate.getName());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        tokenUser.get().setName(userUpdate.getName());
        tokenUser.get().setEmail(userUpdate.getEmail());
        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isBlank()) {
            tokenUser.get().setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }
        userRepository.save(tokenUser.get());
        logger.info("Atualização de usuário realizada. Usuário = {} , UUID = {}"
                , userUpdate.getName(), tokenUser.get().getId());
        return ResponseEntity.ok().build();

    }

    @Transactional
    public ResponseEntity<?> updateUserAsAdmin(User userUpdate, JwtAuthenticationToken token) {

        if (!isUserAnyFieldsValid(userUpdate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> userDb = userRepository.findUserByName(userUpdate.getName());
        if (userDb.isEmpty()) {
            logger.warn("Atualização com nome de usuário inexistente. Usuário = {}", userUpdate.getName());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        if (isUserNameValid(userUpdate)) {
            userDb.get().setName(userUpdate.getName());
        }
        if (isUserEmailValid(userUpdate)) {
            userDb.get().setEmail(userUpdate.getEmail());
        }
        if (isUserPasswordValid(userUpdate)) {
            userDb.get().setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }
        userRepository.save(userDb.get());
        logger.debug("Atualização de usuário realizada como ADMIN. Usuário = {}", userUpdate.getName());
        return ResponseEntity.ok().build();

    }

    @Transactional
    public ResponseEntity<Void> deleteUser(String name, JwtAuthenticationToken token) {

        if (name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

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
                    , name);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Operação de DELETE em usuário inexistente. Usuário a ser deletado = {}"
                    , name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<User> getTokenUserInfo(JwtAuthenticationToken token) {

        Optional<User> tokenUser = userRepository.findById(UUID.fromString(token.getName()));

        if (tokenUser.isPresent()) {
            return ResponseEntity.ok().body(tokenUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    // TODO verificar se necessário escalar verificações em API
    private boolean isUserFieldsValid(User user) {
        return isUserEmailValid(user) && isUserNameValid(user) && isUserPasswordValid(user);
    }

    private boolean isUserAnyFieldsValid(User user) {
        return isUserEmailValid(user) || isUserNameValid(user) || isUserPasswordValid(user);
    }

    private boolean isUserNameValid(User user) {
        return user.getName() != null && !user.getName().isBlank();
    }

    private boolean isUserPasswordValid(User user) {
        return user.getPassword() != null && !user.getPassword().isBlank();
    }

    private boolean isUserEmailValid(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank();
    }

}
