package com.grandhorizon.model;

// Admin is a special user type for system administration tasks, such as managing rooms and bookings.
public class Admin extends User {
    public Admin() {
        setRole("ADMIN");
    }

    public Admin(long id, String firstName, String lastName, String email,
                 String passwordHash, String phone) {
        super(id, firstName, lastName, email, passwordHash, phone, "ADMIN");
    }
}
