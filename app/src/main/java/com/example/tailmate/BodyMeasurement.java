package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BodyMeasurement extends AppCompatActivity {

    TextView name, upd;
    ImageView back;
    FrameLayout frameLayout;

    public String Cid;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_measurement);
        name = findViewById(R.id.naam);
        upd = findViewById(R.id.updation);
        back = findViewById(R.id.back);

        frameLayout = findViewById(R.id.framelayout);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent in = getIntent();
        name.setText(in.getStringExtra("Name").toString());
        Cid = in.getStringExtra("Cid");
        replaceFragment(new Measurements());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimatorSet animatorSet = Animations.backAnimation(back);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        onBackPressed();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }
                });
                animatorSet.start();
            }
        });

        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).collection("Details")
                .document(Cid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        upd.setText("Last Updated On " + documentSnapshot.get("Last Updated On"));
                    }
                });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout,fragment);
        fragmentTransaction.commit();
    }

    private static String hash(String phoneNumber) {
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