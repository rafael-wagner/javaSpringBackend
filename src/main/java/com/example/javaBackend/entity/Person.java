package com.example.javaBackend.entity;

import com.example.javaBackend.entity.jsonview.AddressView;
import com.example.javaBackend.entity.jsonview.PersonView;
import com.example.javaBackend.entity.jsonview.UserView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class Person implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column( name = "id")
    @JsonView(PersonView.Admin.class)
    private Long id;

    @Column(name="name")
    @JsonView(PersonView.Basic.class)
    private String name;

    @Column(name="phone")
    @JsonView(UserView.Basic.class)
    @JsonProperty("phone")
    private String phoneNumber;

    @Column(name="cpf",unique = true)
    @JsonView(PersonView.SelfView.class)
    @JsonProperty("cpf")
    private String cpfNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id",nullable = true)
    @JsonView(AddressView.Basic.class)
    private Address address;

    /*TODO verificar @OneToOne causando FetchType ficar EAGER ao invez de lazy*/
    @OneToOne(fetch=FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id",nullable = true)
    private User user;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCpfNumber() {
        return cpfNumber;
    }

    public void setCpfNumber(String cpfNumber) {
        this.cpfNumber = cpfNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(cpfNumber, person.cpfNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpfNumber);
    }
}
