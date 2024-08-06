package com.example.javaBackend.config;

import com.example.javaBackend.entities.Role;
import com.example.javaBackend.entities.User;
import com.example.javaBackend.repository.RoleRepository;
import com.example.javaBackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Configuration
public class AdminUserConfig implements CommandLineRunner{

    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public AdminUserConfig(
            RoleRepository roleRepository
            , UserRepository userRepository
            , BCryptPasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        Role roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
        Role roleBasic = roleRepository.findByName(Role.Values.BASIC.name());
        Set<Role> roles = Set.of(roleAdmin,roleBasic);

        Optional<User> userAdmin = userRepository.findByName(Role.Values.ADMIN.name());


        userAdmin.ifPresentOrElse(
                (user) -> {
                    System.out.println("admin ja existe");
                },
                () -> {
                    User user = new User();
                    user.setName("admin");
                    user.setPassword(passwordEncoder.encode("123"));
                    user.setRoles(roles);
                    userRepository.save(user);
                }
        );

    }
}
