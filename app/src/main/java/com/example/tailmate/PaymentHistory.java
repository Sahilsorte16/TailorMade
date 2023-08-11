package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PaymentHistory extends AppCompatActivity {

    TextView totalCharges, amountPending;
    RecyclerView rv;
    ImageView back;
    Button payment, bill;
    List<Payment> pays;
    PaymentDetailedAdaptor paymentDetailedAdaptor;

    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    long t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        totalCharges = findViewById(R.id.orderTotal);
        amountPending = findViewById(R.id.amountPending);
        rv = findViewById(R.id.rv);
        payment = findViewById(R.id.payment);
        bill = findViewById(R.id.bill);
        back = findViewById(R.id.back);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        pays = OrderDetails.paymentList;
        paymentDetailedAdaptor = new PaymentDetailedAdaptor(pays, getApplicationContext(), PaymentHistory.this);
        rv.setAdapter(paymentDetailedAdaptor);

        Intent in = getIntent();
        totalCharges.setText(("\u20b9 " + in.getStringExtra("Total Charges")));
        t = Integer.parseInt(in.getStringExtra("Total Charges"));

        amountPending.setText("\u20b9 " + String.valueOf(t - paymentDetailedAdaptor.getTotalPaid()));

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentOptions paymentOptions = new PaymentOptions();
                paymentOptions.show(getSupportFragmentManager(), paymentOptions.getTag());
            }
        });

        bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateBill();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        implementSwipe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null)
        {
            if(requestCode==143)
            {
                signBill(data.getByteArrayExtra("Sign"));
            }
            else if(requestCode==45)
            {
                afterReceivingPayment(data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        OrderDetails.paymentList = pays;
        setResult(RESULT_OK, new Intent());
        finish();
    }

    public void afterReceivingPayment(Intent data)
    {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String amount = data.getStringExtra("Amount");
        String mop = data.getStringExtra("MOP");

        LocalDate currentDate = LocalDate.now();
        String formattedDate = dateTimeFormatter.format(currentDate);
        System.out.println("Current Date: " + formattedDate);

        DayOfWeek currentDay = currentDate.getDayOfWeek();
        String currentDayInitials = currentDay.name().substring(0, 1) + currentDay.name().substring(1).toLowerCase();
        System.out.println("Current Day: " + currentDayInitials);

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedTime = currentTime.format(timeFormatter).replace("am", "AM").replace("pm", "PM");
        System.out.println("Current Time: " + formattedTime);


        System.out.println(OrderDetails.order.getOrderID());
        Payment payment = new Payment();
        payment.setMop(mop);
        payment.setAmount(amount);
        payment.setDate(formattedDate);
        payment.setTime(formattedTime);
        payment.setDay(currentDayInitials);

        paymentDetailedAdaptor.addPayment(payment);
        amountPending.setText("\u20b9 " + String.valueOf(t - paymentDetailedAdaptor.getTotalPaid()));

        firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(OrderDetails.order.getOrderID()).collection("Payment Details")
                .document(payment.getName()).set(payment.makeMap());

    }

    View view;
    ImageView sign;
    PdfDocument pdfDocument;
    int pageWidth, pageHeight;
    private void generateBill() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.bill, null);

        RecyclerView rv = view.findViewById(R.id.recyclerView);
        RecyclerView rv2 = view.findViewById(R.id.recyclerView2);

        TextView tv = view.findViewById(R.id.amountPending);
        TextView text = view.findViewById(R.id.textPaymentSummary);
        TextView shopName, shopAddress, shopEmail, shopPhone, date, cName, orderId, delivery, cPhone, words;
        ImageView pic;

        shopAddress = view.findViewById(R.id.shopAddress);
        shopName = view.findViewById(R.id.shopName);
        shopEmail = view.findViewById(R.id.shopEmail);
        shopPhone = view.findViewById(R.id.shopPhone);
        date = view.findViewById(R.id.date);
        cName = view.findViewById(R.id.cName);
        cPhone = view.findViewById(R.id.cPhone);
        words = view.findViewById(R.id.RupeesInWords);
        orderId = view.findViewById(R.id.orderId);
        delivery = view.findViewById(R.id.orderDelivery);
        pic = view.findViewById(R.id.profilePic);
        sign = view.findViewById(R.id.sign);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        date.setText(dateTimeFormatter.format(LocalDate.now()));
        cName.setText("Customer Name: " + OrderDetails.order.getCustomer().getName());
        cPhone.setText("Customer Phone: " + OrderDetails.order.getCustomer().getMobileNumber());
        orderId.setText("Order ID: " + OrderDetails.order.getOrderID());
        delivery.setText("Delivery Date: " + dateTimeFormatter.format(OrderDetails.order.getDelivery()));

        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv2.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        List<OrderItem> list = OrderDetails.itemlistadaptor.getOrderItems();
        List<Payment> pays = OrderDetails.costDisplayItemView.getPaymentList();

        List<Pair<String,Object>> toBeSent = new ArrayList<>();
        List<Pair<String,Object>> toBeSent2 = new ArrayList<>();

        long total = 0;
        for(OrderItem orderItem: list)
        {
            toBeSent.add(new Pair<>("Item Head", orderItem.getName()));
            long subTotal = 0;
            for(Pair<String,String> p: orderItem.getExpenses())
            {
                toBeSent.add(new Pair<>("Charges", new Pair<>(p.first, "\u20B9 " + p.second)));
                subTotal += Integer.parseInt(p.second);
            }
            long finalSubTotal = subTotal;
            toBeSent.add(new Pair<>("Sub Total", new ArrayList<String>(){{
                add("Total item charges");
                add("\u20B9 " + String.valueOf(finalSubTotal));
                add(orderItem.getQuantity() + " X");
            }}));
            total += subTotal * Integer.parseInt(orderItem.getQuantity());
        }
        toBeSent.add(new Pair<>("Total", new Pair<>("Total order charges", "\u20B9 " + String.valueOf(total))));
        BillAdapter adapter = new BillAdapter(toBeSent, getApplicationContext(), PaymentHistory.this);
        rv.setAdapter(adapter);

        Add_Item.RupeesConverter rupeesConverter = new Add_Item.RupeesConverter();
        String result = rupeesConverter.convertToIndianRupeesWords((int) total);

        words.setText("Rupees in words: " + result);
        for(Payment p: pays)
        {
            toBeSent2.add(new Pair<>("Payment", new Pair<>(p.getName(), "-\u20B9 " + p.getAmount())));
            total -= Integer.parseInt(p.getAmount());
        }

        BillAdapter adapter2 = new BillAdapter(toBeSent2, getApplicationContext(), PaymentHistory.this);
        rv2.setAdapter(adapter2);


        if(toBeSent.isEmpty())
            text.setVisibility(View.GONE);

        tv.setText("\u20B9 " +  String.valueOf(total));

        firebaseFirestore.collection("Shop").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<String> address = (List<String>) documentSnapshot.get("Address");
                        String add = "";
                        for(int i=0; i< address.size(); i++)
                        {
                            if(i== address.size()-1)
                                add += address.get(i);
                            else
                                add += address.get(i) + ", ";
                        }

                        shopAddress.setText(add);
                        shopName.setText(documentSnapshot.get("shopName").toString());
                        shopEmail.setText(documentSnapshot.get("Email").toString());
                        shopPhone.setText(firebaseAuth.getCurrentUser().getPhoneNumber());
                        Uri imageUrl = Uri.parse(documentSnapshot.get("Image").toString());

                        Glide.with(PaymentHistory.this)
                                .load(imageUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .into(pic);


                        pdfDocument = new PdfDocument();
                        pageWidth = 1080;
                        pageHeight = 2000 + toBeSent.size()*25 + toBeSent2.size()*50;
                        continueForward("Bill_" + OrderDetails.order.getOrderName());
                    }
                });
    }

    private void continueForward(String fileName) {


        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, pageWidth, pageHeight);

        Canvas canvas = page.getCanvas();
        view.draw(canvas);

        pdfDocument.finishPage(page);

        File pdfFile = new File(getFilesDir(), fileName + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Writing pdf document failed", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(PaymentHistory.this, Bill_Signature.class);
        intent.putExtra("fileUri", Uri.fromFile(pdfFile));
        startActivityForResult(intent, 143);
    }

    public void signBill(byte[] Signature)
    {
        pdfDocument = new PdfDocument();
        Glide.with(getApplicationContext()).as(byte[].class).load(Signature).addListener(new RequestListener<byte[]>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<byte[]> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(byte[] resource, Object model, Target<byte[]> target, DataSource dataSource, boolean isFirstResource) {
                Bitmap signatureBitmap = BitmapFactory.decodeByteArray(resource, 0, resource.length);

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                int widthSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.EXACTLY);
                view.measure(widthSpec, heightSpec);
                view.layout(0, 0, pageWidth, pageHeight);

                Canvas canvas = page.getCanvas();
                sign.setImageBitmap(signatureBitmap);

                System.out.println(sign.getImageMatrix());
                view.draw(canvas);

                pdfDocument.finishPage(page);
                String fileName = "Bill_" + OrderDetails.order.getOrderName();
                File pdfFile = new File(getFilesDir(), fileName + ".pdf");

                try {
                    pdfDocument.writeTo(new FileOutputStream(pdfFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Writing PDF document failed", Toast.LENGTH_SHORT).show();
                }

                Uri pdfUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.tailmate.fileprovider", pdfFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Handle the exception when a PDF viewer app is not installed
                }

                pdfDocument.close();
                return false;
            }
        }).submit();

    }

    private void implementSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PaymentHistory.this);
                builder.setTitle("Delete Payment")
                        .setIcon(R.drawable.baseline_delete_24)
                        .setMessage("Do you want to delete the payment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                                        .collection("Details").document(OrderDetails.order.getOrderID()).collection("Payment Details")
                                        .document(paymentDetailedAdaptor.pays.get(viewHolder.getLayoutPosition()).getName()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                paymentDetailedAdaptor.removeItem(viewHolder.getLayoutPosition());
                                                amountPending.setText("\u20b9 " + String.valueOf(t - paymentDetailedAdaptor.getTotalPaid()));
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                paymentDetailedAdaptor.notifyItemChanged(viewHolder.getLayoutPosition());
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(PaymentHistory.this, R.color.Delete_red))
                        .addSwipeLeftActionIcon(R.drawable.baseline_delete_24)
                        .addSwipeLeftLabel("Delete Payment")
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(rv);
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