package com.example.tailmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectBodyMeasurements extends AppCompatActivity {

    RecyclerView rv;
    ImageView addBm, back;
    Button lock;
    String Cid;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    List<MeasureCardItem> cardItems;
    Map<String,String> selectedBodyMs;
    BodyMeasurementSelectionAdaptor cardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_body_measurements);

        rv = findViewById(R.id.recyclerView);
        addBm = findViewById(R.id.add_measurement);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        lock = findViewById(R.id.save);
        back = findViewById(R.id.back);

        Intent in = getIntent();
        Cid = in.getStringExtra("Cid");
        selectedBodyMs = (Map<String, String>) in.getSerializableExtra("selectedBodyMs");

        cardItems = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        getBodyMeasurements();

        addBm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCardDialog();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });
    }

    public void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectBodyMeasurements.this);
        LayoutInflater inflater = LayoutInflater.from(SelectBodyMeasurements.this);
        View dialogView = inflater.inflate(R.layout.add_measurement_dialog, null);
        final EditText editTextParameter1 = dialogView.findViewById(R.id.editTextParameter1);
        final EditText editTextParameter2 = dialogView.findViewById(R.id.editTextParameter2);

        builder.setView(dialogView)
                .setTitle("Add New Measurement")
                .setIcon(R.drawable.baseline_add_24)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String parameter1 = editTextParameter1.getText().toString().trim();
                        String parameter2 = editTextParameter2.getText().toString().trim();

                        // Validate the input here and add the new card if valid
                        if (!parameter1.isEmpty() && !parameter2.isEmpty()) {
                            // Create a new CardItem and add it to the list
                            MeasureCardItem newCard = new MeasureCardItem(parameter1, parameter2);
                            newCard.setRemovable(true);
                            cardAdapter.addItem(newCard);
                            //notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void getBodyMeasurements() {
        showLoadingDialog();
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Cid).collection("Body Measurements")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot ds: queryDocumentSnapshots.getDocuments())
                        {
                            MeasureCardItem mci = new MeasureCardItem(ds.get("Title").toString(), ds.get("Length").toString());
                            mci.setRemovable(Boolean.parseBoolean(ds.get("Removable").toString()));
                            if(selectedBodyMs.containsKey(mci.getTitle()))
                                mci.setSelected(true);
                            if(ds.get("ImageUrl") != null)
                                mci.setImageUri(Uri.parse(ds.get("ImageUrl").toString()));
                            cardItems.add(mci);
                        }
                        cardAdapter = new BodyMeasurementSelectionAdaptor(cardItems, getApplicationContext());
                        rv.setAdapter(cardAdapter);
                        dismissLoadingDialog();
                    }
                });
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
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED,intent);
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveChanges() {
        List<MeasureCardItem> bms = cardAdapter.getMeasurementList();
        selectedBodyMs.clear();
        for(MeasureCardItem mci: bms)
        {

            if(mci.isSelected())
            {
                selectedBodyMs.put(mci.getTitle(), mci.getLength());
            }


            Map<String, Object> object1Map = new HashMap<>();
            object1Map.put("Title", mci.getTitle());
            object1Map.put("ImageUrl", mci.getImageUri());
            object1Map.put("Removable", mci.isRemovable());
            object1Map.put("Length", mci.getLength());

            firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                    .collection("Details")
                    .document(Cid).collection("Body Measurements").document(mci.getTitle())
                    .set(object1Map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
        }

        goBack();
    }

    private void goBack() {
        Intent intent = new Intent();
        intent.putExtra("selectedBodyMs", (Serializable) selectedBodyMs);
        setResult(RESULT_OK,intent);
        finish();
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
}