package com.example.tailmate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;

public class CustomerDetails extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 11;
    private static final int REQUEST_CODE_MANAGE_STORAGE_PERMISSION = 12;
    TextView name, phone, Gender;
    ImageView call, msg, back, edit, whatsapp;
    Button measurements, pdf;
    String Cid;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phoneNumber);
        Gender = findViewById(R.id.gender);
        whatsapp = findViewById(R.id.whatsapp);
        call = findViewById(R.id.call);
        msg = findViewById(R.id.msg);
        back = findViewById(R.id.back);
        edit = findViewById(R.id.edit);
        measurements = findViewById(R.id.body_measurements);
        pdf = findViewById(R.id.pdf);

        Glide.with(CustomerDetails.this)
                .load(getResources().getDrawable(R.drawable.whatsapp_logo))
                .apply(RequestOptions.circleCropTransform())
                .into(whatsapp);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent in = getIntent();
        name.setText(in.getStringExtra("Name"));
        phone.setText(in.getStringExtra("Phone"));
        Gender.setText(in.getStringExtra("Gender"));
        Cid = in.getStringExtra("Cid");

        fetchDetails();

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallTheNumber(phone.getText().toString());
            }
        });

        measurements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CustomerDetails.this, BodyMeasurement.class);
                i.putExtra("Name", in.getStringExtra("Name"));
                i.putExtra("Cid", Cid);
                startActivity(i);
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isUserWhatsAppInstalled = isWhatsAppInstalled();
                boolean doesNumberHaveWhatsApp = doesNumberHaveWhatsApp(in.getStringExtra("Phone"));
                if (isUserWhatsAppInstalled && doesNumberHaveWhatsApp) {
                    sendWhatsAppMessage(in.getStringExtra("Phone"), "");
                } else {
                    // Handle if WhatsApp is not installed for either the user or the customer
                }
            }
        });

        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phone.getText().toString();  // Replace with the desired phone number

                // Check if the required permission is granted
                if (ContextCompat.checkSelfPermission(CustomerDetails.this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    openMessagingAppWithNumber(phoneNumber);
                } else {
                    // Request the permission
                    ActivityCompat.requestPermissions(CustomerDetails.this, new String[]{android.Manifest.permission.SEND_SMS}, 2);
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phone.getText().toString();
                String digitsOnly = phoneNumber.replaceAll("\\D","");
                phoneNumber = digitsOnly.substring(Math.max(digitsOnly.length() - 10, 0));

                Intent in = new Intent(CustomerDetails.this, EditCustomer.class);
                in.putExtra("Activity", "Edit Customer");
                in.putExtra("Name", name.getText().toString());
                in.putExtra("Phone", phoneNumber);
                in.putExtra("Gender", Gender.getText().toString());
                in.putExtra("Cid", Cid);
                startActivityForResult(in,178);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimatorSet animatorSet = Animations.backAnimation(back);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        onBackPressed();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }
                });
                animatorSet.start();
            }
        });


        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(ContextCompat.checkSelfPermission(CustomerDetails.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
//                {
//                    Toast.makeText(CustomerDetails.this, "Read permission granted", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(CustomerDetails.this, "Read permission denied", Toast.LENGTH_SHORT).show();
//                    ActivityCompat.requestPermissions(CustomerDetails.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
//                            REQUEST_CODE_STORAGE_PERMISSION);
//                }
                //launchSAFFilePicker();
//                Uri downloadsUri = DocumentsContract.buildDocumentUri(
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "TailMate"
//                        );
//                        System.out.println(downloadsUri+ "**************************************sd************");
                //savePdfFileUsingSAF();
                CustomerPDFGenerator customerPDFGenerator = new CustomerPDFGenerator(CustomerDetails.this);
                customerPDFGenerator.generatePDF(Cid);
            }
        });

    }

    private boolean isWhatsAppInstalled() {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Whatsapp is not installed", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean doesNumberHaveWhatsApp(String phoneNumber) {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(phoneNumber)));
        intent.setPackage("com.whatsapp");
        return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    private void sendWhatsAppMessage(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + phoneNumber + "?text=" + urlEncodeString(message)));
        startActivity(intent);
    }

    public String urlEncodeString(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // Handle the exception based on your application's requirements
            return "";
        }
    }

    private void fetchDetails() {
        showLoadingDialog();
        firebaseFirestore.collection("Customers").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber())).collection("Details")
                .document(Cid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name.setText(documentSnapshot.get("Name").toString());
                        phone.setText(documentSnapshot.get("PhoneNumber").toString());
                        Gender.setText(documentSnapshot.get("Gender").toString());
                        dismissLoadingDialog();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        dismissLoadingDialog();
                    }
                });

    }

    private void CallTheNumber(String phoneNumber) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if it is not granted
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                return;
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));

                startActivity(intent);
            }
        }
    }


    private void openMessagingAppWithNumber(String phoneNumber) {
        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No messaging app found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // The request code should match the one used in requestPermissions()
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, you can now initiate the call
                CallTheNumber(phone.getText().toString());
            } else {
                // Permission is denied
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 2)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the messaging app
                openMessagingAppWithNumber(phone.getText().toString());
            } else {
                Toast.makeText(this, "Permission denied. Cannot open messaging app.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission allowed", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static final int REQUEST_CODE_SAF = 10000;

    private void launchSAFFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "my_pdf_file.pdf");  // Set the MIME type of the content you want to access, such as "image/*" for images
        startActivityForResult(intent, REQUEST_CODE_SAF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SAF && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                // Use the selected file URI for further operations
                // ...
                //savePdfFileUsingSAF(uri);
            }
        }
        else if(requestCode == 178 && resultCode==RESULT_OK)
        {
            fetchDetails();
        }
    }

//    private void savePdfFileUsingSAF(Uri directoryUri) {
//        try {
//            ContentResolver contentResolver = getContentResolver();
//            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(directoryUri, "w");
//
//            if (pfd != null) {
//                FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
//
//                PdfDocument document = new PdfDocument();
//                // Create and format your PDF document here using the PdfDocument API
//
//                // For example, add a blank page
//                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1595, 2842, 1).create();
//                PdfDocument.Page page = document.startPage(pageInfo);
//
//                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View view = inflater.inflate(R.layout.activity_customer_details, null);
//
//
//                int widthSpec = View.MeasureSpec.makeMeasureSpec(1595, View.MeasureSpec.EXACTLY);
//                int heightSpec = View.MeasureSpec.makeMeasureSpec(2842, View.MeasureSpec.EXACTLY);
//                view.measure(widthSpec, heightSpec);
//                view.layout(0, 0, 1595, 2842);
//
//                Canvas canvas = page.getCanvas();
//                view.draw(canvas);
//
//                document.finishPage(page);
//
//                // Write the PDF document to the output stream
//                document.writeTo(fileOutputStream);
//
//                // Close the document and file output stream
//                document.close();
//                fileOutputStream.close();
//                pfd.close();
//
//                System.out.println("File saved in " + directoryUri.toString() + "*****************************");
//
//                Toast.makeText(this, "File saved in " + directoryUri.toString(), Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private ProgressDialog progressDialog;

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
        super.onBackPressed();

    }
}