package com.example.javaBackend.entities;

import com.example.javaBackend.entities.jsonview.View;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name ="role")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column( name = "id")
    private Long id;

    @Column(
            nullable = false
            , name = "name"
            , length = 16
    )
    @JsonView(View.Admin.class)
    private String name;

    @ManyToMany(mappedBy = "roles")
    List<User> users;

    public enum Values {
        ADMIN(1L)
        ,BASIC(2L)
        ;
        final long id;
        Values(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    public Role() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
