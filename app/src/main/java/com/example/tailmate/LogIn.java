package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class LogIn extends AppCompatActivity {

    CountryCodePicker ccp;
    EditText mobile;
    Button otp;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        ccp = (CountryCodePicker) findViewById(R.id.cpp);
        mobile = (EditText) findViewById(R.id.mobile);
        otp = (Button) findViewById(R.id.otp_logIn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ccp.registerCarrierNumberEditText(mobile);

                if(ccp.isValidFullNumber())
                {

                    String number = ccp.getFormattedFullNumber();
                    String hash = generateUniqueID(ccp.getFullNumberWithPlus());
                    System.out.println("+++++++++++++++++++"+ccp.getFullNumberWithPlus()+"++++++++++++++++++++++++");
                    DocumentReference documentRef = firebaseFirestore.collection("Shop").document(hash);
                    Task<DocumentSnapshot> future = documentRef.get();

                    future.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(!documentSnapshot.exists())
                            {
                                Toast.makeText(LogIn.this, "User with this number does not exist", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showLoadingDialog();
                            PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS, LogIn.this,
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            Toast.makeText(LogIn.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                                            dismissLoadingDialog();
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            super.onCodeSent(s, forceResendingToken);
                                            Toast.makeText(LogIn.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();
                                            Intent in = new Intent(LogIn.this, OTP.class);
                                            in.putExtra("Number", number);
                                            in.putExtra("Src", "LogIn");
                                            in.putExtra("VId", s);
                                            startActivity(in);
                                            dismissLoadingDialog();
                                        }
                                    });
                        }
                    });


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

    private ProgressDialog progressDialog;

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private static String generateUniqueID(String phoneNumber) {
        String hash = phoneNumber;

        try {
            // Create an instance of the MD5 hashing algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convert the phone number to bytes and generate the hash
            md.update(phoneNumber.getBytes());
            byte[] digest = md.digest();

            // Convert the byte array to a BigInteger
            BigInteger bigInt = new BigInteger(1, digest);

            // Convert the BigInteger to a hexadecimal string
            hash = bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Handle exceptions related to the hashing algorithm
            e.printStackTrace();
        }

        return hash;
    }
}