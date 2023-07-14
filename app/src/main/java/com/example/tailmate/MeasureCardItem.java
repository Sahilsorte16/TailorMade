package com.example.tailmate;

import android.net.Uri;
import android.widget.EditText;

public class MeasureCardItem {
    private String title = "Measurement Type", Length="0";
    private int imageResId = R.drawable.full_height_f;

    private Uri ImageUri;
    private EditText et;

    private boolean removable, selected;
    public MeasureCardItem(String title) {
        this.title = title;
        removable=false;
    }

    public MeasureCardItem(String title, String length) {
        this.title = title;
        Length = length;
        removable=false;
        selected=false;
    }

    public MeasureCardItem(String title, int img, String length) {
        this.title = title;
        imageResId = img;
        removable=false;
        Length = length;
        selected=false;
    }

    public MeasureCardItem(String title, int img) {
        this.title = title;
        imageResId = img;
        removable=false;
        selected=false;
    }

    public MeasureCardItem(String imageName, Uri uri) {
        title = imageName;
        ImageUri = uri;
        removable=false;
        selected=false;
    }

    public MeasureCardItem(String imageName, Uri uri, String length) {
        title = imageName;
        ImageUri = uri;
        removable=false;
        selected=false;
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

    public EditText getEt() {
        return et;
    }

    public void setEt(EditText et) {
        this.et = et;
    }

    public Uri getImageUri() {
        return ImageUri;
    }

    public void setImageUri(Uri imageUri) {
        ImageUri = imageUri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
