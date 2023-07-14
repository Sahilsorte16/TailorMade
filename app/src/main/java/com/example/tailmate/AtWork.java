package com.example.tailmate;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ernestoyaquello.com.verticalstepperform.Step;

public class AtWork extends Step<String> {

    ImageView back, ahead;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    protected AtWork(String title) {
        super(title);
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View v = layoutInflater.inflate(R.layout.atwork_view, null);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        back = v.findViewById(R.id.goBack);
        ahead = v.findViewById(R.id.goAhead);
        //ahead.setVisibility(View.INVISIBLE);
        return v;
    }


    @Override
    public String getStepData() {
        String str = null;
        if(OrderDetails.order != null)
            str = OrderDetails.order.getDates().get("Prepare Order");
        return str;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return getStepData();
    }

    @Override
    protected void restoreStepData(String data) {

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        return null;
    }


    @Override
    protected void onStepOpened(boolean animated) {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //OrderDetails.verticalStepperForm.markOpenStepAsUncompleted(true, null);
                OrderDetails.verticalStepperForm.goToPreviousStep(true);
            }
        });

        ahead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetails.verticalStepperForm.markOpenStepAsCompleted(true);
                OrderDetails.verticalStepperForm.goToNextStep(true);

                Map<String,String> dates= OrderDetails.order.getDates();

                java.util.Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

                if(dates.get("Payment")==null)
                {
                    dates.put("Payment", dateFormat.format(currentDate));
                    OrderDetails.order.setDates(dates);
                    Map<String, Object> tobeupdated = new HashMap<>();
                    tobeupdated.put("Dates", dates);
                    tobeupdated.put("Status", "Completed");
                    firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                            .collection("Details").document(OrderDetails.order.getOrderID()).update(tobeupdated);
                }
            }
        });
    }

    @Override
    protected void onStepClosed(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

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
