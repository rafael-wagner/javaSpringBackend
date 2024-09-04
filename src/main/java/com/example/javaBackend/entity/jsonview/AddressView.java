package com.example.javaBackend.entity.jsonview;

public interface AddressView {

    interface Basic extends UserView.Basic{}

    interface Detailed extends Basic {}

    interface Admin extends Basic {}

}
