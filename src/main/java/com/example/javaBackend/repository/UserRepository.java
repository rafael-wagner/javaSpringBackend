package com.example.javaBackend.repository;

import com.example.javaBackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User,Long>{
    /**
     * Localiza um usuário pelo name
     * @param username
     * @return Optional<User>
     */
    public Optional<User> findByName(String username);

    /**
     * Localiza um usuário pelo id (UUID) da tabela
     * @param userId
     * @return User
     */
    public User findById(UUID userId);

    /**
     * Busca usuarios pelo nome iniciando com o parametro passado
     * @param userName
     * @return List<User>
     */
    public List<User> findByNameStartingWith(String userName);
}
