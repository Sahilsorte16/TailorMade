package com.example.tailmate;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order implements Parcelable {
    private String OrderName, State, Charges;
    private String OrderID;
    private LocalDate Delivery ;
    private Customer customer;
    private Map<String,String> dates;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private boolean Urgent, Pending;

    public Order(){}

    public Order(String OrderName, LocalDate Delivery, Customer customer)
    {
        this.OrderName = OrderName;
        this.Delivery = Delivery;
        this.customer = customer;
        dates = new HashMap<>();
    }

    public Order(String OrderID, String OrderName, String status, LocalDate Delivery, Customer customer)
    {
        this.OrderID = OrderID;
        this.State = status;
        this.OrderName = OrderName;
        this.Delivery = Delivery;
        this.customer = customer;
        dates = new HashMap<>();
    }

    protected Order(Parcel in) {
        OrderID = in.readString();
        OrderName = in.readString();
        State = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getOrderName() {
        return OrderName;
    }

    public String getOrderID() {
        return OrderID;
    }

    public void setOrderName(String orderName) {
        OrderName = orderName;
    }

    public LocalDate getDelivery() {
        return Delivery;
    }

    public void setDelivery(LocalDate delivery) {
        Delivery = delivery;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(OrderID);
        parcel.writeString(OrderName);
        parcel.writeString(State);
        parcel.writeString(customer.getCid());
        parcel.writeString(customer.getName());
        parcel.writeString(customer.getGender());
        parcel.writeString(Delivery.format(formatter));
        parcel.writeBoolean(Urgent);
    }

    public boolean isUrgent() {
        return Urgent;
    }

    public void setUrgent(boolean urgent) {
        Urgent = urgent;
    }

    public String getCharges() {
        return Charges;
    }

    public void setCharges(String charges) {
        Charges = charges;
    }

    public void setDates(Map<String, String> dates) {
        this.dates = dates;
    }

    public Map<String, String> getDates() {
        return dates;
    }

    public boolean isPending() {
        return Pending;
    }

    public void setPending(boolean pending) {
        Pending = pending;
    }
}
