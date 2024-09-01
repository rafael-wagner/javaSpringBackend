package com.example.javaBackend.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "address")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, name = "postal_code")
    private String postalCode;

    @Column(nullable = true, name = "lane")
    private String lane;

    @Column(nullable = true, name = "city")
    private String city;

    @Column(nullable = true, name = "county")
    private String county;

    @Column(nullable = true, name = "state")
    private String uf;

    @Column(nullable = true, name = "number")
    private String number;

    @Column(nullable = true, name = "country")
    private String country;

    @Column(nullable = true, name = "landmark")
    private String landmark;

    public Address() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id) && Objects.equals(postalCode, address.postalCode) && Objects.equals(lane, address.lane) && Objects.equals(city, address.city) && Objects.equals(county, address.county) && Objects.equals(uf, address.uf) && Objects.equals(number, address.number) && Objects.equals(country, address.country) && Objects.equals(landmark, address.landmark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, postalCode, lane, city, county, uf, number, country, landmark);
    }
}
