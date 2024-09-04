package com.example.javaBackend.entity.jsonview;

public interface PersonView {

    interface Basic extends UserView.Basic{}

    interface SelfView extends PersonView.Basic {}

    interface Admin extends SelfView  {}
}
