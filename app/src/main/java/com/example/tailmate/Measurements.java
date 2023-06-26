package com.example.tailmate;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Measurements extends Fragment {

    private RecyclerView recyclerView;
    private MeasureCardAdapter cardAdapter;
    private ActionMode actionMode;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    List<MeasureCardItem> cardItems;
    boolean editable;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_measurements, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        // Prepare sample data for the cards
        cardItems = new ArrayList<>();

        // Add more card items as needed
        String cid;
        editable = true;
        if(getActivity() instanceof BodyMeasurement)
        {
            editable = false;
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            cid = ((BodyMeasurement) getActivity()).Cid;
            setValues(cid);
        }
        else
        {
            EditCustomer editCustomer = (EditCustomer) getActivity();
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            if(editCustomer.activity.equals("Add Customer"))
            {
                cardItems.add(new MeasureCardItem(getString(R.string.full_height),R.drawable.full_height_f));
                cardItems.add(new MeasureCardItem(getString(R.string.waist_to_floor), R.drawable.waist_to_floor_f));
                cardItems.add(new MeasureCardItem(getString(R.string.waist_to_knee), R.drawable.waist_to_knee_f));
                cardItems.add(new MeasureCardItem(getString(R.string.waist_to_hip), R.drawable.waist_to_hip_f));
                cardItems.add(new MeasureCardItem(getString(R.string.Shoulder_to_Waist), R.drawable.shoulder_to_waist_f));
                cardItems.add(new MeasureCardItem(getString(R.string.Shoulder), R.drawable.shoulder_f));
                cardItems.add(new MeasureCardItem(getString(R.string.Bust_height), R.drawable.bust_height_f));
                cardItems.add(new MeasureCardItem(getString(R.string.Croch_to_Knee), R.drawable.croch_to_knee_f));
                cardItems.add(new MeasureCardItem(getString(R.string.leg_length),R.drawable.leg_length_f));
                cardItems.add(new MeasureCardItem(getString(R.string.body_rise), R.drawable.body_rise_f));
                cardItems.add(new MeasureCardItem(getString(R.string.Head), R.drawable.head_f));
                cardItems.add(new MeasureCardItem(getString(R.string.neck_size), R.drawable.neck_size_f));
                cardItems.add(new MeasureCardItem(getString(R.string.Arm_length), R.drawable.arm_length_f));

                cardAdapter = new MeasureCardAdapter(cardItems, getContext(), editable);
                recyclerView.setAdapter(cardAdapter);
            }
            else
            {
                cid = ((EditCustomer) getActivity()).Cid;
                setValues(cid);
            }

        }



        return v;
    }

    private void setValues(String cid) {
        showLoadingDialog();
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(cid).collection("Body Measurements")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot ds: queryDocumentSnapshots.getDocuments())
                        {
                            MeasureCardItem mci = new MeasureCardItem(ds.get("Title").toString(),Integer.parseInt(ds.get("Image").toString()),
                                    ds.get("Length").toString());
                            mci.setRemovable(Boolean.parseBoolean(ds.get("Removable").toString()));
                            cardItems.add(mci);

                        }
                        cardAdapter = new MeasureCardAdapter(cardItems, getContext(), editable);
                        recyclerView.setAdapter(cardAdapter);
                        dismissLoadingDialog();
                    }
                });
    }

    public void MeasurementData(String cid)
    {
        List<MeasureCardItem> fetchedData = cardAdapter.getMeasurementList();
        List<String> Ids = new ArrayList<>();

        for(MeasureCardItem m: fetchedData)
        {
            Ids.add(m.getTitle());
        }
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details")
                .document(cid).collection("Body Measurements").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentReference> tobeDeleted = new ArrayList<>();
                        for(DocumentSnapshot ds: queryDocumentSnapshots)
                        {
                            if(!Ids.contains(ds.getId()))
                                ds.getReference().delete();
                        }

                        /*System.out.println("***************************************");
                        for (DocumentReference dr: tobeDeleted)
                        {
                            System.out.println(dr.getId());
                        }
                        System.out.println("***************************************");*/
                    }
                });

        for(MeasureCardItem mci: fetchedData)
        {
            Map<String, Object> object1Map = new HashMap<>();
            object1Map.put("Title", mci.getTitle());
            object1Map.put("Image", mci.getImageResId());
            object1Map.put("Removable", mci.isRemovable());
            object1Map.put("Length", mci.getLength());
            System.out.println(mci.getTitle() + " " + mci.getLength());
            firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                    .collection("Details")
                    .document(cid).collection("Body Measurements").document(mci.getTitle())
                    .set(object1Map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
        }
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