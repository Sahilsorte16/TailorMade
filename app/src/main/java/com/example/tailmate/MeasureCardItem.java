package com.example.tailmate;

public class MeasureCardItem {
    private String title = "Measurement Type", Length="0";
    private int imageResId = R.drawable.full_height_f;

    private boolean removable;
    public MeasureCardItem(String title) {
        this.title = title;
        removable=false;
    }

    public MeasureCardItem(String title, int img) {
        this.title = title;
        imageResId = img;
        removable=false;
    }
    public String getTitle() {
        return title;
    }

    public String getLength() {
        return Length;
    }

    public void setLength(String length) {
        Length = length;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }
}
