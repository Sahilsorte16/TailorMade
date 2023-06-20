package com.example.tailmate;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class Customers extends Fragment {
    RecyclerView recyclerView;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int REQUEST_SELECT_CONTACT = 101;
    CustomerAdapter customerAdapter;
    ImageView addButton;
    SearchView sv;
    List<Customer> customerList;
    Intent in;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_customers, container, false);

        recyclerView = v.findViewById(R.id.CustomerList);
        addButton = v.findViewById(R.id.add_customer);
        sv = v.findViewById(R.id.search_view_customers);

        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        customerList = new ArrayList<>();

        customerList.add(new Customer("John Doe", "1234567890"));
        customerList.add(new Customer("Alice Smith", "9876543210"));
        customerList.add(new Customer("Alice Smith", "9876543210"));
        customerList.add(new Customer("Alice Smith", "9876543210"));
        customerList.add(new Customer("Alice Smith", "9876543210"));
        customerList.add(new Customer("Bob Johnson", "9876543210"));

        customerAdapter = new CustomerAdapter(customerList, getContext());
        recyclerView.setAdapter(customerAdapter);

        addCustomer();
        in = new Intent(getActivity(), EditCustomer.class);
        in.putExtra("Activity", "Add Customer");
        in.putExtra("Name", "");
        in.putExtra("Phone", "");

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Customer> filteredCustomerList = new ArrayList<>();
                for (Customer item : customerList) {
                    if(item.getName().toUpperCase().contains(s.toUpperCase()) || item.getMobileNumber().contains(s))
                    {
                        filteredCustomerList.add(item);
                    }
                }

                CustomerAdapter filteredCustomerAdapter = new CustomerAdapter(filteredCustomerList, getContext());
                recyclerView.setAdapter(filteredCustomerAdapter);

                return true;
            }
        });
        return v;
    }

    public void addCustomer()
    {

// Create a popup menu and associate it with the ImageView
        PopupMenu popupMenu = new PopupMenu(getContext(), addButton, Gravity.TOP);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popupMenu.getMenu());

// Handle the popup menu item selection
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.manually:
                        startActivity(in);
                        return true;
                    case R.id.contacts:
                        readContact();
                        return true;
                    default:
                        return false;
                }
            }
        });

// Show the popup menu when the ImageView is clicked
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    private void readContact() {
        int permissionCheck = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, REQUEST_SELECT_CONTACT);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContact();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();

            if (contactUri != null) {
                Cursor cursor = requireActivity().getContentResolver().query(contactUri, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String name = cursor.getString(nameIndex);
                    int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String phoneNumber = cursor.getString(phoneNumberIndex);

                    // Add name and phone number to the list
                    //System.out.println(name + " - " + phoneNumber+"++++++++++++");
                    customerList.add(new Customer(name, phoneNumber));
                    in.putExtra("Name", name);
                    in.putExtra("Phone", phoneNumber);
                    cursor.close();
                    customerAdapter.notifyDataSetChanged();
                    startActivity(in);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        customerAdapter = new CustomerAdapter(customerList, getContext());
        recyclerView.setAdapter(customerAdapter);
    }
}