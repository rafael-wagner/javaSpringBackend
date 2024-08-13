package com.example.javaBackend.repository;

import com.example.javaBackend.entities.Role;
import com.example.javaBackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User,UUID>{
    /**
     * Localiza um usuário pelo name
     * @param userName nome de usuário
     * @return Optional <User> um usuário de nome igual ao param
     */
    @Query("select u from User u where u.name = :userName")
    public Optional<User> findUserByName(String userName);

    /**
     *
     * @param name nome para busca
     * @return List User de usuários com nome similar(like)
     */
    @Query("select u from User u where u.name like :name%")
    public List<User> findByName(String name);

    /**
     *
     * @param email email de usuário
     * @return List de User
     */
    List<User> findByEmail(String email);

    /**
     *
     * @param name nome de usuário
     * @param email email de usuário
     * @return List de User
     */
    @Query("select u from User u where u.name like %:name% and u.email like :email% order by u.name")
    List<User> findByNameAndEmail(String name, String email);

    @Query("select u from User u join u.roles r where r = :role")
    List<User> findByRole(Role role);

    @Query("select u from User u")
    List<User> findAll();

}
