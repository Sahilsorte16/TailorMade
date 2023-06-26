package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditCustomer extends AppCompatActivity {

    EditText name, phone;
    TextView tv;
    ImageView back;
    Button save;
    RadioGroup rg;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String gender, Cid;
    public String activity;
    RadioButton f, m;
    CountryCodePicker ccp;
    Measurements measurements;
    boolean add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phoneNumber);
        back = findViewById(R.id.back);
        tv = findViewById(R.id.textView6);
        ccp = findViewById(R.id.cpp);
        save = findViewById(R.id.save);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        gender = "Female";
        add = false;
        Cid = null;
        Intent in = getIntent();
        activity = in.getStringExtra("Activity").toString();
        String naam = in.getStringExtra("Name").toString();
        String number = in.getStringExtra("Phone").toString();

        tv.setText(activity);

        if(activity.equals("Edit Customer"))
        {
            gender = in.getStringExtra("Gender").toString();
            Cid = in.getStringExtra("Cid").toString();
        }

        if(!naam.isEmpty())
            name.setText(naam);

        if(!number.isEmpty())
            phone.setText(number);


        measurements = new Measurements();
        replaceFragment(measurements);

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
        f = findViewById(R.id.female);
        m = findViewById(R.id.male);

        if(gender.equals("Female"))
            f.setChecked(true);
        else
            m.setChecked(true);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.female:
                        gender = "Female";
                        break;
                    case R.id.male:
                        gender = "Male";
                        break;
                }
            }
        });


    }

    private void saveChanges() {
        String Name = name.getText().toString();
        String Phone = phone.getText().toString();
        ccp.registerCarrierNumberEditText(phone);
        if(Name.isEmpty() || Phone.isEmpty())
        {
            Toast.makeText(this, "Enter all the details", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!ccp.isValidFullNumber())
        {
            Toast.makeText(this, "Invalid Mobile Number", Toast.LENGTH_SHORT).show();
            return;
        }

        Phone = ccp.getFullNumberWithPlus();

        String Uid = hash(firebaseAuth.getCurrentUser().getPhoneNumber());

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        Map<String, Object> additions = new HashMap<>();
        additions.put("Name", Name);
        additions.put("PhoneNumber", Phone);
        additions.put("Gender", gender);
        additions.put("Last Updated On", formattedDate);

        firebaseFirestore.collection("Customers").document(Uid).collection("Details")
                .whereEqualTo("PhoneNumber", Phone)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> customers = queryDocumentSnapshots.getDocuments();

                        if(Cid==null)
                        {
                            if(!customers.isEmpty())
                            {
                                Toast.makeText(EditCustomer.this, "Customer with this phone Number already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else
                            {
                                addCustomer(additions);
                            }
                        }
                        else
                        {
                            if(!customers.isEmpty() && !Cid.equals(customers.get(0).getId()))
                            {
                                Toast.makeText(EditCustomer.this, "Customer with this phone Number already exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else
                            {
                                updateCustomer(additions);
                            }
                        }

                        goBack();
                    }
                });
    }

    private void addCustomer(Map<String,Object> additions) {
        //showLoadingDialog();
        Cid = hash(additions.get("PhoneNumber").toString());
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details")
                .document(Cid)
                .set(additions).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        addBodyMeasurements();
                        Toast.makeText(EditCustomer.this, "Customer added", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCustomer(Map<String,Object> additions) {
        //showLoadingDialog();
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details")
                .document(Cid)
                .update(additions).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditCustomer.this, "Customer updated", Toast.LENGTH_SHORT).show();
                        addBodyMeasurements();
                        //dismissLoadingDialog();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCustomer.this, "Customer not updated", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });

    }

    private void addBodyMeasurements() {
        measurements.MeasurementData(Cid);
        return;
    }


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
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
                        goBack();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goBack() {

        Intent in = new Intent();
        setResult(RESULT_OK, in);
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