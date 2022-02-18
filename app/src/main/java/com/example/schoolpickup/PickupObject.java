package com.example.schoolpickup;

public class PickupObject {

    private String name, ic, classroom, gender, pickupStatus, vehiclePlateNo;

    private PickupObject() {}

    private PickupObject(String name, String ic, String classroom, String gender, String pickupStatus, String vehiclePlateNo) {
        this.name = name;
        this.ic = ic;
        this.classroom = classroom;
        this.gender = gender;
        this.pickupStatus = pickupStatus;
        this.vehiclePlateNo = vehiclePlateNo;
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

    public String getPickupStatus() {
        return pickupStatus;
    }

    public String getVehiclePlateNo() {
        return vehiclePlateNo;
    }
}
