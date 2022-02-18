package com.example.schoolpickup;

public class StudentObject {

    private String name, ic, classroom, gender;

    private StudentObject() {}

    private StudentObject(String name, String ic, String classroom, String gender) {
        this.name = name;
        this.ic = ic;
        this.classroom = classroom;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public String getIC() {
        return ic;
    }

    public String getClassroom() {
        return classroom;
    }

    public String getGender() {
        return gender;
    }
}
