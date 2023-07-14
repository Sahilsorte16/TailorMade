package com.example.tailmate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ORDER = 0;
    private static final int VIEW_TYPE_DATE = 1;
    List<Object> orderLists;
    Context context;
    Activity activity;
    Fragment fragment;

    public OrderListAdapter(List<Order> orderLists, Context context, Activity activity, Fragment parentFragment)
    {
        this.orderLists = getSortedOrder(orderLists);
        this.context = context;
        this.activity = activity;
        this.fragment = parentFragment;
    }

    private List<Object> getSortedOrder(List<Order> orderLists) {

        List<Object> tobeReturned = new ArrayList<>();
        LocalDate currentDate = null;
        Collections.sort(orderLists, Comparator.comparing(Order::getDelivery).reversed().thenComparing(Order::isUrgent).reversed());
        for(Order order: orderLists)
        {
            LocalDate deliver = order.getDelivery();
            if (!deliver.equals(currentDate)) {
                tobeReturned.add(String.valueOf(deliver.format(DateTimeFormatter.ofPattern("MMMM dd"))));
                System.out.println(String.valueOf(deliver.format(DateTimeFormatter.ofPattern("MMMM dd"))));
                currentDate = deliver;
            }
            System.out.println(order.getCustomer().getName() + " " + order.getOrderName() + " " + order.isUrgent());
            tobeReturned.add(order);
        }

        return tobeReturned;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = orderLists.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_DATE;
        } else if (item instanceof Customer) {
            return VIEW_TYPE_ORDER;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if(viewType==VIEW_TYPE_ORDER)
        {
            View view = inflater.inflate(R.layout.order_item, parent, false);
            return new OrderListAdapter.OrderListItemCard(view);
        }
        else if(viewType == VIEW_TYPE_DATE)
        {
            View view = inflater.inflate(R.layout.date, parent, false);
            return new OrderListAdapter.OrderDateItemCard(view);
        }

        throw new IllegalArgumentException("Invalid view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = orderLists.get(position);

        if(holder instanceof OrderListItemCard)
        {
            OrderListItemCard orderListItemCard = (OrderListItemCard) holder;
            orderListItemCard.bind((Order)item, context);
            orderListItemCard.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OrderDetails.class);
                    intent.putExtra("Oid", ((Order) item).getOrderID());
                    fragment.startActivityForResult(intent, 871);
                }
            });
        }
        else if(holder instanceof OrderDateItemCard)
        {
            OrderDateItemCard orderDateItemCard = (OrderDateItemCard) holder;
            orderDateItemCard.bind((String) item);
        }

    }

    @Override
    public int getItemCount() {
        return orderLists.size();
    }

    static class OrderListItemCard extends RecyclerView.ViewHolder {

        ImageView orderPic;
        TextView CustomerName, OrderName, Urgent;
        FirebaseStorage firebaseStorage;
        FirebaseAuth firebaseAuth;

        OrderListItemCard(View view) {
            super(view);
            orderPic = view.findViewById(R.id.orderImage);
            CustomerName = view.findViewById(R.id.customer_name);
            OrderName = view.findViewById(R.id.order_name);
            Urgent = view.findViewById(R.id.URGENT);
            firebaseStorage = FirebaseStorage.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
        }

        public void bind(Order order, Context context) {
            Customer customer = order.getCustomer();
            StorageReference sref = firebaseStorage.getReference().child(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).child("Customers").child(customer.getCid()).child(order.getOrderID());
            sref.child("Item 1").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    if(listResult.getItems().size()>0)
                    {
                        StorageReference firstImageRef = listResult.getItems().get(0);
                        firstImageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(context)
                                                .load(uri)
                                                .transform(new CenterCrop(), new RoundedCorners(20))
                                                .into(orderPic);
                                    }
                                });
                    }
                }
            });
            CustomerName.setText(customer.getName());
            OrderName.setText(order.getOrderName());
            if(order.isUrgent())
                Urgent.setVisibility(View.VISIBLE);
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

    private class OrderDateItemCard extends RecyclerView.ViewHolder {

        TextView date;
        public OrderDateItemCard(View view) {
            super(view);
            date = view.findViewById(R.id.dateText);
        }

        public void bind(String item) {
            date.setText(item);
            date.setPaintFlags(date.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}
