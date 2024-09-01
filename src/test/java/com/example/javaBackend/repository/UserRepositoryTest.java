package com.example.javaBackend.repository;

import com.example.javaBackend.entity.Role;
import com.example.javaBackend.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    /**
     * Encontrar usuário pelo nome
     * ENTRADA: nome de usuário
     * SAÍDA: Um usuário com nome IGUAL ao nome de entrada
     * REQUISITOS: registro de usuário na tabela users com nome válido
     */
    @Test
    void findUserByName() {
        User user = getUserFromDb();
        String nameSearch = user.getName();
        Optional<User> userTest = userRepository.findUserByName(nameSearch);
        userTest.ifPresentOrElse(
                u -> assertEquals(u.getName(), nameSearch)
                , () -> Assertions.fail("Usuário não foi encontrado")
        );

    }

    /**
     * Encontrar usuário pelo email
     * ENTRADA: email de usuário
     * SAÍDA: Um usuário com email contento o 'email' de entrada
     * REQUISITOS: registro de usuário na tabela users com email válido
     */
    @Test
    void findByEmail() {
        User user = getUserFromDb();
        String email = user.getEmail();
        List<User> userTest = userRepository.findByEmail(email);
        userTest.forEach(
                u -> assertTrue(u.getEmail().contains(email))
        );

    }

    /**
     * Encontrar usuário pelo nome e email
     * ENTRADA: email e nome de usuário
     * SAÍDA: Um usuário com nome e email contendo os parametro de entrada entrada
     * REQUISITOS: registro de usuário na tabela users com email e nome válidos
     */
    @Test
    void findByNameAndEmail() {
        User user = getUserFromDb();
        String email = user.getEmail();
        String name = user.getName();
        List<User> userTest = userRepository.findByNameAndEmail(name, email);
        userTest.forEach(
                u -> assertTrue(
                        u.getName().contains(name)
                                && u.getEmail().contains(email)
                )
        );
    }

    /**
     * Encontrar usuário pela Role
     * ENTRADA: Role para busca em bd
     * SAÍDA: Um usuário a Role de entrada
     * REQUISITOS: registro de usuário na tabela com role válida
     */
    @Test
    void findByRole() {

        Role roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());

        List<User> users = userRepository.findByRole(roleAdmin);

        users.forEach(u-> assertTrue(
                u.getRoles().stream().anyMatch(r -> r.equals(roleAdmin))
        ));


    }

    /**
     *  Realiza consulta list de usuários em BD
     *  e retorna o primeiro usuário encontrado na lista
     * @return User
     */
    private User getUserFromDb() {
        List<User> userList = userRepository.findAll();
        if (userList.isEmpty()) {
            Assertions.fail("Usuário não foi encontrado usuário pelo método 'findAll()'");
        }
        return userList.get(0);
    }
}