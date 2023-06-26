package com.example.tailmate;

public class Customer {
    private String name;
    private String mobileNumber;


    private String gender, lastUpadated;
    private String Cid;

    public Customer(String name, String mobileNumber) {
        this.name = name;
        this.mobileNumber = mobileNumber;
    }

    public Customer(String name, String mobileNumber,String gender, String Cid) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.gender = gender;
        this.Cid = Cid;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCid() {
        return Cid;
    }

    public void setCid(String cid) {
        Cid = cid;
    }

    public String getLastUpadated() {
        return lastUpadated;
    }

    public void setLastUpadated(String lastUpadated) {
        this.lastUpadated = lastUpadated;
    }
}