package com.example.tailmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class SignUp extends AppCompatActivity {

    EditText name, email, phone;
    Button getOtp;
    CountryCodePicker cpp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = (EditText) findViewById(R.id.Name);
        email= (EditText) findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.mobile);
        getOtp = (Button) findViewById(R.id.get_otp);
        cpp = (CountryCodePicker) findViewById(R.id.cpp);

        getOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phone.getText().toString();
                String Name = name.getText().toString();
                String eMail = email.getText().toString();
                if(Name.isEmpty() || phoneNumber.isEmpty() || eMail.isEmpty())
                {
                    Toast.makeText(SignUp.this, "Enter all the details.", Toast.LENGTH_SHORT).show();
                }
                else{
                    cpp.registerCarrierNumberEditText(phone);
                    if(cpp.isValidFullNumber())
                    {
                        String number = cpp.getFormattedFullNumber();
                        Intent in = new Intent(SignUp.this, OTP.class);
                        in.putExtra("Number", number);
                        in.putExtra("Src", "SignUp");
                        startActivity(in);
                    }
                    else
                    {
                        Toast.makeText(SignUp.this, "Invalid Mobile Number.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUp.this, MainActivity.class));
    }

}