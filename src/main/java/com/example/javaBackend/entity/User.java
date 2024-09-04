package com.example.javaBackend.entity;

import com.example.javaBackend.entity.jsonview.UserView;
import com.example.javaBackend.entity.jsonview.PersonView;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity()
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonView(UserView.Admin.class)
    private UUID id;

    @Column(
            nullable = false
            , name = "name"
            , length = 16
            , unique = true
    )

    @JsonView({
            UserView.Basic.class
            , UserView.LoginCredentials.class
    })
    private String name;

    @JsonView(UserView.LoginCredentials.class)
    @Column(
            nullable = false
            , name = "password"
            , length = 64
    )
    private String password;

    @Column(name = "email", unique = true)
    @JsonView(UserView.Basic.class)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role"
            , joinColumns = @JoinColumn(name = "user_id")
            , inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonView(UserView.Admin.class)
    private Set<Role> roles;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    @JsonView(PersonView.Basic.class)
    private Person person;

    public User() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
