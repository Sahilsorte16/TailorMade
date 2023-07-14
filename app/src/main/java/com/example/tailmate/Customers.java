package com.example.tailmate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class Customers extends Fragment {
    public RecyclerView recyclerView;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int REQUEST_SELECT_CONTACT = 101;
    CustomerAdapter customerAdapter;
    ImageView addButton;
    SearchView sv;
    List<Customer> customerList;
    TextView tv;
    Intent in;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_customers, container, false);

        recyclerView = v.findViewById(R.id.CustomerList);
        addButton = v.findViewById(R.id.add_customer);
        sv = v.findViewById(R.id.search_view_customers);
        tv = v.findViewById(R.id.nothing);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        customerList = new ArrayList<>();
        tv.setVisibility(View.VISIBLE);
        fetchCustomers();

        if(getActivity() instanceof HomePage)
        {
            //implementSwipe();
        }

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

                CustomerAdapter filteredCustomerAdapter = new CustomerAdapter(filteredCustomerList, getContext(),recyclerView, Customers.this);
                recyclerView.setAdapter(filteredCustomerAdapter);

                return true;
            }
        });
        return v;
    }

    private void implementSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ){
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction==ItemTouchHelper.LEFT)
                {
                    if(viewHolder instanceof CustomerAdapter.CustomerViewHolder)
                    {
                        Customer customer = ((CustomerAdapter.CustomerViewHolder) viewHolder).getCustomer();
                        List<MeasureCardItem> cardItems = new ArrayList<>();
                        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                                .collection("Details").document(customer.getCid()).collection("Body Measurements")
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (DocumentSnapshot ds: queryDocumentSnapshots.getDocuments())
                                        {
                                            MeasureCardItem mci = new MeasureCardItem(ds.get("Title").toString(),Integer.parseInt(ds.get("Image").toString()),
                                                    ds.get("Length").toString());
                                            mci.setRemovable(Boolean.parseBoolean(ds.get("Removable").toString()));
                                            cardItems.add(mci);


                                            ds.getReference().delete();

                                        }
                                    }
                                });


                        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                                .collection("Details").document(customer.getCid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        int position = viewHolder.getAdapterPosition(); // Get the position of the item

                                        if (customerAdapter != null && position != RecyclerView.NO_POSITION) {
                                            customerAdapter.removeItems(position); // Remove the item from the adapter// Notify the adapter that the item has been removed
                                        }
                                    }
                                });

                        Snackbar.make(recyclerView, customer.getName() + " deleted", Snackbar.LENGTH_SHORT)
                                .setAction( "UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        setFirestore(customer, cardItems);
                                        fetchCustomers();
                                    }
                                }).show();
                    }
                }


            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if(viewHolder instanceof CustomerAdapter.CustomerViewHolder)
                {

                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Delete_red))
                            .addSwipeLeftActionIcon(R.drawable.baseline_delete_24)
                            .addSwipeLeftLabel("Delete Customer")
                            .create()
                            .decorate();




                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void setFirestore(Customer customer, List<MeasureCardItem> cardItems) {
        Map<String, Object> additions = new HashMap<>();
        additions.put("Name", customer.getName());
        additions.put("PhoneNumber", customer.getMobileNumber());
        additions.put("Gender", customer.getGender());
        additions.put("Last Updated On", customer.getLastUpadated());
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(customer.getCid())
                .set(additions);

        CollectionReference cref = firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(customer.getCid()).collection("Body Measurements");

        for(MeasureCardItem mci: cardItems)
        {
            Map<String, Object> object1Map = new HashMap<>();
            object1Map.put("Title", mci.getTitle());
            object1Map.put("Image", mci.getImageResId());
            object1Map.put("Removable", mci.isRemovable());
            object1Map.put("Length", mci.getLength());
            cref.document(mci.getTitle()).set(object1Map);
        }
    }

    public void fetchCustomers() {
        showLoadingDialog();
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        customerList.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String customerId = documentSnapshot.getId();
                            String customerName = documentSnapshot.getString("Name");
                            String phoneNumber = documentSnapshot.getString("PhoneNumber");
                            String gender = documentSnapshot.getString("Gender");
                            Customer c = new Customer(customerName, phoneNumber, gender, customerId);
                            c.setLastUpadated(documentSnapshot.get("Last Updated On").toString());
                            customerList.add(c);
                        }

                        //Toast.makeText(getContext(), String.valueOf(customerList.size()), Toast.LENGTH_SHORT).show();

                        if(!customerList.isEmpty())
                            tv.setVisibility(View.INVISIBLE);


                        customerAdapter = new CustomerAdapter(customerList, getContext(), recyclerView, Customers.this);
                        recyclerView.setAdapter(customerAdapter);
                        dismissLoadingDialog();
                    }
                });
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
                        startActivityForResult(in, 178);
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
                    String digitsOnly = phoneNumber.replaceAll("\\D","");
                    phoneNumber = digitsOnly.substring(Math.max(digitsOnly.length() - 10, 0));

                    // Add name and phone number to the list
                    //System.out.println(name + " - " + phoneNumber+"++++++++++++");

                    customerList.add(new Customer(name, phoneNumber));
                    in.putExtra("Name", name);
                    in.putExtra("Phone", phoneNumber);
                    cursor.close();
                    customerAdapter.notifyDataSetChanged();
                    startActivityForResult(in,178);
                }
            }
        }
        else if(requestCode==178 && resultCode == Activity.RESULT_OK){
            fetchCustomers();
        }
        else if(requestCode==871){

            if(resultCode == Activity.RESULT_OK)
            {
                Intent intent = new Intent();
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
            else {
                Intent intent = new Intent();
                getActivity().setResult(Activity.RESULT_CANCELED, intent);
                getActivity().finish();
            }
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        customerAdapter = new CustomerAdapter(customerList, getContext());
//        recyclerView.setAdapter(customerAdapter);
//    }
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