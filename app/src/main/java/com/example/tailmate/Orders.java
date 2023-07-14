package com.example.tailmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Orders extends Fragment {

    TabLayout tabLayout;
    ViewPager viewPager;
    ImageView addOrder;

    MyPagerAdapter adapter;

    public List<Order> upcoming, active, completed, delivered;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_orders, container, false);

        upcoming = new ArrayList<>();
        active =new ArrayList<>();
        completed = new ArrayList<>();
        delivered = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        tabLayout = v.findViewById(R.id.tabs);
        viewPager = v.findViewById(R.id.viewPager);
        addOrder = v.findViewById(R.id.add_order);


        fetchOrders();
        addOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), SelectCustomer.class), 871);
            }
        });
        return v;
    }

    public void fetchOrders() {

        showLoadingDialog();
        CollectionReference cref = firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details");

        firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot ds: queryDocumentSnapshots.getDocuments())
                        {
                            cref.document(ds.get("Cid").toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot d) {
                                    Customer customer = new Customer(d.get("Name").toString(), d.get("PhoneNumber").toString(),
                                            d.get("Gender").toString(), d.getId());

                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

                                    String status = ds.get("Status").toString();
                                    Order order = new Order(ds.getId(), ds.get("Order Name").toString(), status
                                            , LocalDate.parse(ds.get("Delivery Date").toString(), formatter), customer);
                                    order.setUrgent((Boolean) ds.get("Urgent"));
                                    if(status.equals("Upcoming")) upcoming.add(order);
                                    else if(status.equals("Active")) active.add(order);
                                    else if(status.equals("Completed")) completed.add(order);
                                    else if(status.equals("Delivered")) delivered.add(order);

                                    if (upcoming.size() + active.size() + completed.size() + delivered.size() == queryDocumentSnapshots.size()) {
                                        adapter = new MyPagerAdapter(getChildFragmentManager());
                                        viewPager.setAdapter(adapter);
                                        viewPager.setOffscreenPageLimit(4);
                                        tabLayout.setupWithViewPager(viewPager);

                                        dismissLoadingDialog();
                                    }
                                }
                            });

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissLoadingDialog();
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            QuerySnapshot document = task.getResult();
                            if (document!=null && !document.isEmpty()) {}
                            else {
                                Toast.makeText(getContext(), "No orders yet", Toast.LENGTH_SHORT).show();
                                dismissLoadingDialog();
                            }
                        }

                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==871 && resultCode == Activity.RESULT_OK)
        {
            upcoming.clear();
            active.clear();
            completed.clear();
            delivered.clear();
            fetchOrders();
        }
    }

    private String hash(String phoneNumber) {
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
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private Integer[] tabTitles = {R.string.Upcoming, R.string.Active, R.string.Completed, R.string.Delivered};

        public MyPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            OrderList fragment = new OrderList();
            Bundle bundle = new Bundle();

            switch (position)
            {
                case 1:
                    bundle.putString("tab", "Active");
                    bundle.putParcelableArrayList("cardItems", (ArrayList<? extends Parcelable>) active);
                    fragment.setArguments(bundle);
                    return fragment;
                case 2:
                    bundle.putString("tab", "Completed");
                    bundle.putParcelableArrayList("cardItems", (ArrayList<? extends Parcelable>) completed);
                    fragment.setArguments(bundle);
                    return fragment;
                case 3:
                    bundle.putString("tab", "Delivered");
                    bundle.putParcelableArrayList("cardItems", (ArrayList<? extends Parcelable>) delivered);
                    fragment.setArguments(bundle);
                    return fragment;
                default:
                    bundle.putString("tab", "Upcoming");
                    bundle.putParcelableArrayList("cardItems", (ArrayList<? extends Parcelable>) upcoming);
                    fragment.setArguments(bundle);
                    return fragment;
            }
        }
        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getString(tabTitles[position]);
        }



    }


}