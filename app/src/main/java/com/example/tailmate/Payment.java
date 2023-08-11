package com.example.tailmate;

import java.util.HashMap;
import java.util.Map;

public class Payment {

    String name, mop, date, time, day, amount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMop() {
        return mop;
    }

    public void setMop(String mop) {
        this.mop = mop;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Map<String,String> makeMap()
    {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("MOP", getMop());
        fieldMap.put("Date", getDate());
        fieldMap.put("Time", getTime());
        fieldMap.put("Day", getDay());
        fieldMap.put("Amount", getAmount());

        return fieldMap;
    }
}
