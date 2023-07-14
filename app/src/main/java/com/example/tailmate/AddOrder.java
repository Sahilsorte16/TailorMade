package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddOrder extends AppCompatActivity {

    EditText orderName;
    TextView orderId, CName, CNumber, CGender, AddItem, date, totalAmt, header;
    Switch urgent;
    LinearLayout delivery;
    Button save;
    ImageView back;
    RecyclerView recyclerView;
    public String Cid, Oid;
    boolean Urgent;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    List<OrderItem> list;
    ItemListAdaptor itemlistadaptor;
    Order order;
   int totalImageUploads=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        header = findViewById(R.id.textView6);
        orderName = findViewById(R.id.orderName);
        orderId = findViewById(R.id.orderId);
        CName = findViewById(R.id.customerName);
        CNumber = findViewById(R.id.customerPhone);
        CGender = findViewById(R.id.customerGender);
        AddItem = findViewById(R.id.addItem);
        save = findViewById(R.id.save);
        back = findViewById(R.id.back);
        date = findViewById(R.id.editTextDate);
        delivery = findViewById(R.id.DeliveryDate);
        urgent = findViewById(R.id.switch1);
        totalAmt = findViewById(R.id.totalAmount);

        recyclerView = findViewById(R.id.itemListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemlistadaptor = new ItemListAdaptor(getApplicationContext(), AddOrder.this, true);
        recyclerView.setAdapter(itemlistadaptor);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        list = new ArrayList<>();

        Intent intent = getIntent();


        if(intent.getStringExtra("Activity").equals("Edit Order"))
        {
            header.setText("Edit Order");
            Oid = intent.getStringExtra("Oid");
            fetchDetails();
        }
        else
        {
            generateOrderId();
            setCustomer(intent);
        }
        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
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
        AddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(AddOrder.this, Add_Item.class);
                intent.putExtra("Cid", Cid);
                intent.putExtra("activity","Add Item");
                intent.putExtra("LayoutPosition", -1);
                startActivityForResult(intent, 123);
            }
        });

    }

    private void setCustomer(Intent intent) {
        CName.setText(intent.getStringExtra("Name"));
        CNumber.setText(intent.getStringExtra("Phone"));
        CGender.setText(intent.getStringExtra("Gender"));
        Cid = intent.getStringExtra("Cid");
    }

    private void generateOrderId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String str = now.format(formatter)+firebaseAuth.getCurrentUser().getPhoneNumber();
        Oid = hash(str);
        orderId.setText("Order Id: " + Oid);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddOrder.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        LocalDate Date = LocalDate.of(year, month + 1, dayOfMonth); // Month value is 0-based, so add 1 to the month

                        // Format the date using the desired pattern
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                        String formattedDate = Date.format(formatter);
                        date.setText(formattedDate);
                    }
                },
                year,
                month,
                dayOfMonth
        );

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK && data!=null)
        {
            OrderItem orderItem = new OrderItem();
            orderItem.setName(data.getStringExtra("Item Name"));
            orderItem.setType(data.getStringExtra("Item type"));
            orderItem.setCharges(data.getStringExtra("Charges"));
            orderItem.setTotalItemCharges(data.getStringExtra("Total amount"));
            orderItem.setBodyMs((Map<String, String>) data.getSerializableExtra("Body Measurements"));
            orderItem.setInstructions(data.getStringArrayListExtra("Instructions"));
            orderItem.setClothImages((ArrayList<byte[]>) data.getSerializableExtra("Cloth Images"));
            orderItem.setPatternImages((ArrayList<byte[]>) data.getSerializableExtra("Pattern Images"));
            orderItem.print();

            if(data.getIntExtra("LayoutPosition",-1)!=-1)
            {
                itemlistadaptor.updateItem(orderItem, data.getIntExtra("LayoutPosition",-1));
            }
            else
                itemlistadaptor.addItem(orderItem);

            totalAmt.setText("₹ " + itemlistadaptor.makeAmount());
        }
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

    private void saveChanges() {
        String order_name = orderName.getText().toString();
        String Date = date.getText().toString();
        List<OrderItem> list = itemlistadaptor.getOrderItems();
        Urgent = urgent.isChecked();
        if(order_name.isEmpty())
        {
            Toast.makeText(this, "Giver your Order Name", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(Date.isEmpty())
        {
            Toast.makeText(this, "Set Delivery Date", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(list.isEmpty())
        {
            Toast.makeText(this, "You have not added items", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference sref = firebaseStorage.getReference().child(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).child("Customers").child(Cid).child(Oid);
        DocumentReference dref = firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Oid);

        List<Task<?>> deletionTasks = new ArrayList<>();

        showLoadingDialog();
        dref.collection("Item Details").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Task<Void> documentDeletionTask = dref.collection("Item Details").document(document.getId()).delete();
                        deletionTasks.add(documentDeletionTask);

                        Task<Void> storageDeletionTask = sref.child(document.getId()).listAll()
                                .onSuccessTask(new SuccessContinuation<ListResult, Void>() {
                                    @NonNull
                                    @Override
                                    public Task<Void> then(ListResult listResult) throws Exception {
                                        List<StorageReference> storageReferences = listResult.getItems();
                                        List<Task<Void>> deleteTasks = new ArrayList<>();
                                        for (StorageReference storageReference : storageReferences) {
                                            deleteTasks.add(storageReference.delete());
                                        }
                                        return Tasks.whenAll(deleteTasks);
                                    }
                                });
                        deletionTasks.add(storageDeletionTask);
                    }
                    Task<Void> allDeletionTasks = Tasks.whenAll(deletionTasks);
                    allDeletionTasks.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            proceedUpdation();
                        }
                    });
                }
            }
        });
       
    }

    int totalItems ;
    int completedItems = 0;
    int completedImageUploads = 0;
    private void proceedUpdation() {
        String order_name = orderName.getText().toString();
        String Date = date.getText().toString();
        List<OrderItem> list = itemlistadaptor.getOrderItems();
        Urgent = urgent.isChecked();

        StorageReference sref = firebaseStorage.getReference().child(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).child("Customers").child(Cid).child(Oid);
        DocumentReference dref = firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Oid);

        java.util.Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        Map<String,Object> addition = new HashMap<>();
        addition.put("Order Name", order_name);
        addition.put("Cid", Cid);
        addition.put("Delivery Date", Date);
        addition.put("Urgent", Urgent);
        addition.put("Status", "Upcoming");
        addition.put("Total Amount", totalAmt.getText().toString());

        if(header.getText().equals("Edit Order"))
        {
            dref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Map<String,String> dates = new HashMap<>();
                    dates = (Map<String, String>) documentSnapshot.get("Dates");
                    addition.put("Dates", dates);
                    proceedUpdation1(addition);
                }
            });
        }
        else
        {
            Map<String,String> dates = new HashMap<>();
            dates.put("Received", dateFormat.format(currentDate));
            dates.put("Prepare Order", null);
            dates.put("Payment", null);
            dates.put("Delivered", null);
            addition.put("Dates", dates);
            proceedUpdation1(addition);
        }
    }

    private void proceedUpdation1(Map<String, Object> addition) {
        String order_name = orderName.getText().toString();
        String Date = date.getText().toString();
        List<OrderItem> list = itemlistadaptor.getOrderItems();
        Urgent = urgent.isChecked();

        StorageReference sref = firebaseStorage.getReference().child(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).child("Customers").child(Cid).child(Oid);
        DocumentReference dref = firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Oid);

        for(int i=0; i<list.size(); i++)
        {
            totalImageUploads += list.get(i).getPatternImages().size() + list.get(i).getClothImages().size();
        }
        totalItems = list.size();

        dref.set(addition).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Uri orderImage = null;
                for(int i=0; i<list.size(); i++)
                {
                    int k = 1;
                    for(byte[] arr: list.get(i).getClothImages())
                    {
                        sref.child("Item " + String.valueOf(i+1)).child("Cloth " + String.valueOf(k++)).putBytes(arr)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        completedImageUploads++;
                                        if(completedImageUploads==totalImageUploads)
                                            checkCompletion();
                                    }
                                });

                    }

                    k=1;
                    for(byte[] arr: list.get(i).getPatternImages())
                    {
                        sref.child("Item " + String.valueOf(i+1)).child("Pattern " + String.valueOf(k++)).putBytes(arr)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        completedImageUploads++;
                                        if(completedImageUploads==totalImageUploads)
                                            checkCompletion();
                                    }
                                });
                    }

                    Map<String,Object> m = new HashMap<>();
                    m.put("Item Name", list.get(i).getName());
                    m.put("Item Type", list.get(i).getType());
                    m.put("Charges", list.get(i).getCharges());
                    m.put("Total amount", list.get(i).getCharges());
                    m.put("Expenses", null);
                    m.put("isComplete", false);
                    m.put("Instructions", list.get(i).getInstructions());
                    m.put("Body Measurements", list.get(i).getBodyMs());

                    dref.collection("Item Details").document("Item " + String.valueOf(i+1)).set(m)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    completedItems++;

                                    // Check if all items are completed
                                    if (completedItems == totalItems) {
                                        Map<String, Object> rec = new HashMap<>();
                                        LocalDate currentDate = LocalDate.now();
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
                                        rec.put("Date",  currentDate.format(formatter));
                                        checkCompletion();
                                    }
                                }
                            });

                }
            }
        });

    }

    private void checkCompletion() {
        if(completedItems==totalItems && completedImageUploads==totalImageUploads)
        {
            dismissLoadingDialog();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void fetchDetails() {
        DocumentReference dref = firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Oid);
        showLoadingDialog();
        dref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot ds) {
                firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                        .collection("Details").document(ds.get("Cid").toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot d) {
                                Customer customer = new Customer(d.get("Name").toString(), d.get("PhoneNumber").toString(),
                                        d.get("Gender").toString(), d.getId());
                                Cid = d.getId();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                                String status = ds.get("Status").toString();
                                order = new Order(ds.getId(), ds.get("Order Name").toString(), status
                                        , LocalDate.parse(ds.get("Delivery Date").toString(), formatter), customer);
                                order.setUrgent((Boolean) ds.get("Urgent"));
                                order.setCharges(ds.get("Total Amount").toString());

                                orderName.setText(order.getOrderName());
                                orderId.setText("Order Id: "+order.getOrderID());
                                CName.setText(customer.getName());
                                CGender.setText(customer.getGender());
                                CNumber.setText(customer.getMobileNumber());
                                date.setText(ds.get("Delivery Date").toString());
                                totalAmt.setText(order.getCharges());

                                fetchItems();
                            }
                        });

            }
        });
    }

    private void fetchItems() {
        itemlistadaptor = new ItemListAdaptor(getApplicationContext(), AddOrder.this, true);
        recyclerView.setAdapter(itemlistadaptor);
        StorageReference sref = firebaseStorage.getReference().child(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).child("Customers")
                .child(order.getCustomer().getCid()).child(order.getOrderID());

        firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Oid).collection("Item Details")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot ds: queryDocumentSnapshots)
                        {
                            OrderItem orderItem = new OrderItem();
                            orderItem.setName((String) ds.get("Item Name"));
                            orderItem.setType((String) ds.get("Item Type"));
                            orderItem.setInstructions((List<String>) ds.get("Instructions"));
                            orderItem.setCharges((String) ds.get("Charges"));
                            orderItem.setTotalItemCharges((String) ds.get("Total amount"));
                            orderItem.setBodyMs((Map<String, String>) ds.get("Body Measurements"));

                            sref.child(ds.getId()).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    ArrayList<byte[]> cloths = new ArrayList<>();
                                    ArrayList<byte[]> pattern = new ArrayList<>();

                                    for(StorageReference item : listResult.getItems())
                                    {
                                        item.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                String itemName = item.getName();
                                                if(itemName.contains("Cloth"))
                                                {
                                                    cloths.add(bytes);
                                                }
                                                else
                                                {
                                                    pattern.add(bytes);
                                                }

                                                if(cloths.size() + pattern.size() == listResult.getItems().size())
                                                {
                                                    orderItem.setPatternImages(pattern);
                                                    orderItem.setClothImages(cloths);

                                                    itemlistadaptor.addItem(orderItem);
                                                    if(itemlistadaptor.getItemCount()==queryDocumentSnapshots.getDocuments().size())
                                                    {
                                                        totalAmt.setText("₹ " + itemlistadaptor.makeAmount());
                                                        dismissLoadingDialog();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
    }


    private void goBack() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED,intent);
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
        progressDialog = new ProgressDialog(AddOrder.this);
        progressDialog.setMessage("Saving Details...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}