package com.example.tailmate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class EditCustomer extends AppCompatActivity {

    EditText name, phone;
    TextView tv;
    ImageView back;
    Button save;
    RadioGroup rg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phoneNumber);
        back = findViewById(R.id.back);
        tv = findViewById(R.id.textView6);
        save = findViewById(R.id.save);

        Intent in = getIntent();
        tv.setText(in.getStringExtra("Activity").toString());

        String naam = in.getStringExtra("Name").toString();
        String number = in.getStringExtra("Phone").toString();

        if(!naam.isEmpty())
        name.setText(naam);

        if(!number.isEmpty())
        phone.setText(number);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });

        rg = findViewById(R.id.gender);
        RadioButton f = findViewById(R.id.female);
        RadioButton m = findViewById(R.id.male);
        f.setChecked(true);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.female:

                        break;
                    case R.id.male:

                        break;
                }
            }
        });
    }

    private void saveChanges() {

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save changes")
                .setMessage("Do you want to save the changes?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges();
                        goBack();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goBack();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goBack() {
        super.onBackPressed();
    }
}