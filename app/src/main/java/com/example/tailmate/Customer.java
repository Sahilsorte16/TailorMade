package com.example.tailmate;

public class Customer {
    private String name;
    private String mobileNumber;

    private String gender;

    public Customer(String name, String mobileNumber) {
        this.name = name;
        this.mobileNumber = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
}