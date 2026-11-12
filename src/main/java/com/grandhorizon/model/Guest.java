package com.grandhorizon.model;

// Guest represents the normal end-user account used for registration, booking, and profile management.
public class Guest extends User {
    public Guest() {
        setRole("GUEST");
    }

    public Guest(long id, String firstName, String lastName, String email,
                 String passwordHash, String phone) {
        super(id, firstName, lastName, email, passwordHash, phone, "GUEST");
    }
}
