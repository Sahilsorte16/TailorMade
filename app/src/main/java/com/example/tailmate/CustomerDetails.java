package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomerDetails extends AppCompatActivity {

    TextView name, phone, Gender;
    ImageView call, msg, back, edit;
    Button measurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phoneNumber);
        Gender = findViewById(R.id.gender);
        call = findViewById(R.id.call);
        msg = findViewById(R.id.msg);
        back = findViewById(R.id.back);
        edit = findViewById(R.id.edit);
        measurements = findViewById(R.id.body_measurements);

        Intent in = getIntent();
        name.setText(in.getStringExtra("Name"));
        phone.setText(in.getStringExtra("Phone"));

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallTheNumber(phone.getText().toString());
            }
        });

        measurements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CustomerDetails.this, BodyMeasurement.class);
                i.putExtra("Name", in.getStringExtra("Name"));
                startActivity(i);
            }
        });

        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phone.getText().toString();  // Replace with the desired phone number

                // Check if the required permission is granted
                if (ContextCompat.checkSelfPermission(CustomerDetails.this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    openMessagingAppWithNumber(phoneNumber);
                } else {
                    // Request the permission
                    ActivityCompat.requestPermissions(CustomerDetails.this, new String[]{android.Manifest.permission.SEND_SMS}, 2);
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(CustomerDetails.this, EditCustomer.class);
                in.putExtra("Activity", "Edit Customer");
                in.putExtra("Name", name.getText().toString());
                in.putExtra("Phone", phone.getText().toString());
                startActivity(in);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void CallTheNumber(String phoneNumber) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if it is not granted
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                return;
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));

                startActivity(intent);
            }
        }
    }


    private void openMessagingAppWithNumber(String phoneNumber) {
        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No messaging app found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // The request code should match the one used in requestPermissions()
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, you can now initiate the call
                CallTheNumber(phone.getText().toString());
            } else {
                // Permission is denied
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 2)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the messaging app
                openMessagingAppWithNumber(phone.getText().toString());
            } else {
                Toast.makeText(this, "Permission denied. Cannot open messaging app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}