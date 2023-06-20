package com.example.tailmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class LogIn extends AppCompatActivity {

    CountryCodePicker ccp;
    EditText mobile;
    Button otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        ccp = (CountryCodePicker) findViewById(R.id.cpp);
        mobile = (EditText) findViewById(R.id.mobile);
        otp = (Button) findViewById(R.id.otp_logIn);



        otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ccp.registerCarrierNumberEditText(mobile);

                if(ccp.isValidFullNumber())
                {
                    String number = ccp.getFormattedFullNumber();
                    Intent in = new Intent(LogIn.this, OTP.class);
                    in.putExtra("Number", number);
                    in.putExtra("Src", "LogIn");
                    startActivity(in);
                }
                else
                {
                    Toast.makeText(LogIn.this, "Invalid Mobile Number.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LogIn.this, MainActivity.class));
    }
}