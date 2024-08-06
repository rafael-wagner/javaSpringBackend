package com.example.javaBackend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Customer")
public class Customer extends Person {
}
