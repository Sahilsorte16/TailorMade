package com.example.tailmate;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ernestoyaquello.com.verticalstepperform.Step;

public class Steps extends Step<String> {

    Order order;
    Button button1;
    LinearLayout linearLayout;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    String str = null;
    public Steps(String stepTitle) {
        super(stepTitle);
    }


    @Override
    protected View createStepContentLayout() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(16, 16, 16, 16);

        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText("Order has been received successfully !");
        textView.setTextSize(15);


        button1 = new Button(getContext());
        LinearLayout.LayoutParams btn1params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        btn1params.setMargins(0,20,0,5);
        button1.setLayoutParams(btn1params);
        button1.setPadding(30,0,30,0);
        button1.setText("Start working on the order");
        button1.setTextSize(12);
        button1.setTextColor(getContext().getColor(R.color.white));
        button1.setCompoundDrawablePadding(3);
        button1.setBackgroundColor(getContext().getColor(R.color.default_dark));




        linearLayout.addView(textView);
        linearLayout.addView(button1);

        return linearLayout;
    }

    @Override
    public String getStepData() {
//        if(str==null)
//        {
//            java.util.Date currentDate = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
//            return dateFormat.format(currentDate);
//        }
        if(OrderDetails.order != null)
            str = OrderDetails.order.getDates().get("Received");
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
        return new IsDataValid(true, null);
    }

    @Override
    protected void onStepOpened(boolean animated) {
        int n = OrderDetails.verticalStepperForm.getOpenStepPosition();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderDetails.verticalStepperForm.markOpenStepAsCompleted(true);
                OrderDetails.verticalStepperForm.goToNextStep(true);
                Map<String,String> dates= OrderDetails.order.getDates();

                java.util.Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

                if(dates.get("Prepare Order")==null)
                {
                    dates.put("Prepare Order", dateFormat.format(currentDate));
                    OrderDetails.order.setDates(dates);
                    Map<String, Object> tobeupdated = new HashMap<>();
                    tobeupdated.put("Dates", dates);
                    tobeupdated.put("Status", "Active");
                    firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                            .collection("Details").document(OrderDetails.order.getOrderID()).update(tobeupdated);
                }


                //button1.setVisibility(View.GONE);

            }
        });
        System.out.println("Bool: "  + OrderDetails.verticalStepperForm.areAllPreviousStepsCompleted(n));
    }

    @Override
    protected void onStepClosed(boolean animated) {
        if(button1.getVisibility()!=View.GONE)
            OrderDetails.verticalStepperForm.markOpenStepAsCompleted(false);
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        //OrderDetails.verticalStepperForm.goToNextStep(true);

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
    private ProgressDialog progressDialog;

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
