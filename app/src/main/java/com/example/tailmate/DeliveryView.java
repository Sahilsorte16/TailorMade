package com.example.tailmate;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import ernestoyaquello.com.verticalstepperform.Step;

public class DeliveryView extends Step<String> {

    protected DeliveryView(String title) {
        super(title);
    }

    TextView tv;
    Button button;
    ImageView back;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    @Override
    protected View createStepContentLayout() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.delivery_view, null, false);
        tv = v.findViewById(R.id.tv);
        button= v.findViewById(R.id.journey);
        back = v.findViewById(R.id.goBack);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        return v;
    }

    @Override
    public String getStepData() {
        String str = null;
        if(OrderDetails.order != null)
            str = OrderDetails.order.getDates().get("Delivered");
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
        String str = "Order Delivered Successfully!\nOrder delivered on " + OrderDetails.order.getDates().get("Delivered") +
                ".\nThe amount pending for order is ";
        System.out.println(OrderDetails.order.getDates());
        String amount = OrderDetails.completionPayment.amtPending.getText().toString();

        tv.setText(str + amount);
        if(amount.equals("\u20b9 0"))
            button.setVisibility(View.VISIBLE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetails.verticalStepperForm.goToPreviousStep(true);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetails.verticalStepperForm.markOpenStepAsCompleted(true);
                OrderDetails.verticalStepperForm.completeForm();
                OrderDetails.order.setPending(false);
                Map<String, Object> tobeupdated = new HashMap<>();
                tobeupdated.put("Pending", false);
                firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                        .collection("Details").document(OrderDetails.order.getOrderID()).update(tobeupdated);
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
