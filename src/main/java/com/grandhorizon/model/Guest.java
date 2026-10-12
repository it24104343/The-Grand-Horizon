package com.grandhorizon.model;

public class Guest extends User {
    public Guest() {
        setRole("GUEST");
    }

    public Guest(long id, String firstName, String lastName, String email,
                 String passwordHash, String phone) {
        super(id, firstName, lastName, email, passwordHash, phone, "GUEST");
    }
}
