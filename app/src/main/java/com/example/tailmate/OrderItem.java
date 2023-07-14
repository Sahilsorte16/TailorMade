package com.example.tailmate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Pair;

public class OrderItem {
    private String id, name, type, charges, totalItemCharges;
    private Map<String,String> bodyMs;

    private List<Pair<String,String>> expenses;
    private List<String> instructions;
    private ArrayList<byte[]> ClothImages, PatternImages, DressImages;
    private boolean isComplete=false;

    public OrderItem()
    {
        instructions = new ArrayList<>();
        expenses = new ArrayList<>();
        ClothImages = new ArrayList<>();
        PatternImages = new ArrayList<>();
        DressImages = new ArrayList<>();
        totalItemCharges = charges;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCharges() {
        return charges;
    }

    public void setCharges(String charges) {
        this.charges = charges;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public ArrayList<byte[]> getClothImages() {
        return ClothImages;
    }

    public void setClothImages(ArrayList<byte[]> clothImages) {
        ClothImages = clothImages;
    }

    public ArrayList<byte[]> getPatternImages() {
        return PatternImages;
    }

    public void setPatternImages(ArrayList<byte[]> patternImages) {
        PatternImages = patternImages;
    }

    public Map<String, String> getBodyMs() {
        return bodyMs;
    }

    public void setBodyMs(Map<String, String> bodyMs) {
        this.bodyMs = bodyMs;
    }

    public void print() {
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
        System.out.println("Charges: " + charges);

        System.out.println("Instructions:");
        if (instructions != null) {
            for (String instruction : instructions) {
                System.out.println("- " + instruction);
            }
        }

        System.out.println("Body Measurements:");
        if (bodyMs != null) {
            for (Map.Entry<String, String> entry : bodyMs.entrySet()) {
                System.out.println("- " + entry.getKey() + ": " + entry.getValue());
            }
        }

        System.out.println("Cloth Images:");
        if (ClothImages != null) {
            for (byte[] image : ClothImages) {
                System.out.println("- " + Arrays.toString(image));
            }
        }

        System.out.println("Pattern Images:");
        if (PatternImages != null) {
            for (byte[] image : PatternImages) {
                System.out.println("- " + Arrays.toString(image));
            }
        }
    }

    public List<Pair<String, String>> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Pair<String, String>> expenses) {
        this.expenses = expenses;
    }

    public ArrayList<byte[]> getDressImages() {
        return DressImages;
    }

    public void setDressImages(ArrayList<byte[]> dressImages) {
        DressImages = dressImages;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getTotalItemCharges() {
        return totalItemCharges;
    }

    public void setTotalItemCharges(String totalItemCharges) {
        this.totalItemCharges = totalItemCharges;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
