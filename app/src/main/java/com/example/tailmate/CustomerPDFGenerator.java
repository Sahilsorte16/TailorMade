package com.example.tailmate;

import static androidx.core.content.ContextCompat.createDeviceProtectedStorageContext;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CustomerPDFGenerator {
    int pageWidth = 1080;
    int pageHeight = 1920;
    Context context;
    View view;
    PdfDocument pdfDocument;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    public CustomerPDFGenerator(Context context)
    {
        this.context = context;
    }
    public void generatePDF(String Cid) {
        pdfDocument = new PdfDocument();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.customer_pdf, null);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        createView(Cid);
    }

    private void createView(String Cid) {
        TextView shop, owner, mobile, email, ad1, ad2;
        ImageView iv;
        shop = view.findViewById(R.id.shop_name);
        owner = view.findViewById(R.id.owner);
        mobile = view.findViewById(R.id.mobile);
        email = view.findViewById(R.id.email);
        ad1 = view.findViewById(R.id.ad1);
        ad2 = view.findViewById(R.id.ad2);
        iv = view.findViewById(R.id.profilePic);

        firebaseFirestore.collection("Shop").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot ds) {
                        shop.setText(ds.get("shopName").toString());
                        owner.setText(ds.get("Owner").toString());
                        mobile.setText(firebaseAuth.getCurrentUser().getPhoneNumber());
                        email.setText(ds.get("Email").toString());

                        Glide.with(context)
                                .load(Uri.parse(ds.get("Image").toString()))
                                .apply(RequestOptions.circleCropTransform())
                                .into(iv);

                        List<String> l = (List<String>) ds.get("Address");

                        System.out.println(l.toString() + "**************************************************");
                        ad1.setText(l.get(0));
                        String str = "";
                        for(int i=1; i<l.size()-1; i++)
                        {
                            str += l.get(i)+",";
                        }
                        str += l.get(l.size()-1);
                        ad2.setText(str);

                        setCustomerDetails(Cid);
                    }
                });
    }

    private void setCustomerDetails(String Cid) {
        TextView name, mobile, gender, updation;
        ImageView iv;
        name = view.findViewById(R.id.cName);
        mobile = view.findViewById(R.id.cMobile);
        gender = view.findViewById(R.id.cGender);
        updation = view.findViewById(R.id.cUpdation);

        DocumentReference dref = firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                .collection("Details").document(Cid);

        dref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name.setText("Name: "+documentSnapshot.get("Name").toString());
                mobile.setText("Phone Number: " + documentSnapshot.get("PhoneNumber").toString());
                gender.setText("Gender: "+documentSnapshot.get("Gender").toString());
                updation.setText("Last Updated On: "+documentSnapshot.get("Last Updated On").toString());

                setBodyMeasurements(Cid);

            }
        });

    }

    private void setBodyMeasurements(String cid) {
        List<MeasureCardItem> cardItems = new ArrayList<>();
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(view.getContext()));
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
                        MeasureCardAdapter cardAdapter = new MeasureCardAdapter(cardItems, context, false);
                        pageHeight += cardItems.size()*100 + 200;
                        rv.setAdapter(cardAdapter);
                        continueForward(cid);
                    }
                });
    }

    private void continueForward(String Cid) {

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, pageWidth, pageHeight);

        System.out.println(view.getHeight());

        Canvas canvas = page.getCanvas();
        view.draw(canvas);

        pdfDocument.finishPage(page);

        File pdfFile = new File(context.getFilesDir(), Cid + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Writing pdf document failed", Toast.LENGTH_SHORT).show();
        }

        Uri pdfUri = FileProvider.getUriForFile(context, "com.example.tailmate.fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle the exception when a PDF viewer app is not installed
        }


        pdfDocument.close();
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
