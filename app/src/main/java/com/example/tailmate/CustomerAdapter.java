package com.example.tailmate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LETTER = 0;
    private static final int VIEW_TYPE_CUSTOMER = 1;

    private List<Object> itemList;
    private List<Customer> customerList;
    RecyclerView recylerView;
    Context context;
    Fragment fragment;

    public CustomerAdapter(List<Customer> customerList, Context context, RecyclerView rv, Fragment fragment) {
        itemList = generateItemList(customerList);
        this.customerList = customerList;
        this.context = context;
        this.fragment = fragment;
        recylerView = rv;
    }

    private List<Object> generateItemList(List<Customer> customerList) {
        List<Object> itemList = new ArrayList<>();

        // Sort the customer list alphabetically
        Collections.sort(customerList, new Comparator<Customer>() {
            @Override
            public int compare(Customer customer1, Customer customer2) {
                return customer1.getName().compareToIgnoreCase(customer2.getName());
            }
        });

        char currentLetter = ' ';
        for (Customer customer : customerList) {
            char firstLetter = Character.toUpperCase(customer.getName().charAt(0));
            if (firstLetter != currentLetter) {
                if(firstLetter>='A' && firstLetter<='Z')
                itemList.add(String.valueOf(firstLetter));
                else
                    itemList.add("#");
                currentLetter = firstLetter;
            }
            itemList.add(customer);
        }

        return itemList;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = itemList.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_LETTER;
        } else if (item instanceof Customer) {
            return VIEW_TYPE_CUSTOMER;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_LETTER) {
            View view = inflater.inflate(R.layout.item_letter, parent, false);
            return new LetterViewHolder(view);
        } else if (viewType == VIEW_TYPE_CUSTOMER) {
            View view = inflater.inflate(R.layout.item_customer, parent, false);
            return new CustomerViewHolder(view);
        }
        else{}

        throw new IllegalArgumentException("Invalid view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = itemList.get(position);

        if (holder instanceof LetterViewHolder) {
            LetterViewHolder letterViewHolder = (LetterViewHolder) holder;
            letterViewHolder.bind((String) item);
        } else if (holder instanceof CustomerViewHolder) {
            CustomerViewHolder customerViewHolder = (CustomerViewHolder) holder;
            customerViewHolder.bind((Customer) item);

            Customer c = customerViewHolder.getCustomer();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CustomerDetails.class);
                    intent.putExtra("Name", c.getName());
                    intent.putExtra("Phone", c.getMobileNumber());
                    intent.putExtra("Gender", c.getGender());
                    intent.putExtra("Cid", c.getCid());
                    fragment.startActivityForResult(intent,178);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void removeItems(int position)
    {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }


    static class LetterViewHolder extends RecyclerView.ViewHolder {
        private TextView letterTextView;

        LetterViewHolder(@NonNull View itemView) {
            super(itemView);
            letterTextView = itemView.findViewById(R.id.text_letter);
        }

        void bind(String letter) {
            letterTextView.setText(letter);
        }
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView mobileNumberTextView;
        public Customer customer;
        CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.customer_name);
            mobileNumberTextView = itemView.findViewById(R.id.customer_mobile);
        }

        void bind(Customer customer) {
            nameTextView.setText(customer.getName());
            this.customer=customer;
            mobileNumberTextView.setText(customer.getMobileNumber());
        }

        public Customer getCustomer() {
            return customer;
        }
    }
}
