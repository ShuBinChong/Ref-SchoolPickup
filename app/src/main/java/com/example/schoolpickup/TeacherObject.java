package com.example.schoolpickup;

public class TeacherObject {

    private String name, email, contact;

    private TeacherObject() {}

    private TeacherObject(String name, String email, String contact) {
        this.name = name;
        this.email = email;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getContact() {
        return contact;
    }
}
