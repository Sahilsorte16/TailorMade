package com.example.tailmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class OrderList extends Fragment {

    RecyclerView rv;
    List<Order> orderLists;

    public List<Order> upcoming, active, completed, delivered;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_order_list, container, false);

        upcoming = new ArrayList<>();
        active =new ArrayList<>();
        completed = new ArrayList<>();
        delivered = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        rv = v.findViewById(R.id.orderRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(v.getContext()));


        orderLists = new ArrayList<>();
        String tab = "qwerty";
        Bundle args = getArguments();
        if (args != null && args.containsKey("cardItems"))
        {
            tab = (String) args.getString("tab");
            ArrayList<? extends Parcelable> parcelableArrayList = args.getParcelableArrayList("cardItems");
            orderLists = new ArrayList<>(parcelableArrayList.size());
            for (Parcelable parcelable : parcelableArrayList) {
                if (parcelable instanceof Order) {
                    orderLists.add((Order) parcelable);
                }
            }
            OrderListAdapter orderListAdapter = new OrderListAdapter(orderLists, getContext(), getActivity(), getParentFragment());
            rv.setAdapter(orderListAdapter);
        }
        return v;
    }

}