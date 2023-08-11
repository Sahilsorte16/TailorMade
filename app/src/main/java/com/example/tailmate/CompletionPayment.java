package com.example.tailmate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ernestoyaquello.com.verticalstepperform.Step;

public class CompletionPayment extends Step<String> {

    RecyclerView rv;
    TextView amtPending, notifyText;
    Activity activity;
    Button takePay, bill;
    FloatingActionButton notify;
    ImageButton whatsapp, msg, call;
    ImageView back, ahead;
    LinearLayout ll;
    private boolean isExpanded = false;
    private String message_matter;
    FragmentManager fragmentManager;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    private PdfDocument pdfDocument;
    private int pageHeight;
    private int pageWidth;

    //ImageView qr;
    protected CompletionPayment(String title, Activity activity, FragmentManager fragmentManager) {
        super(title);
        this.activity = activity;
        this.fragmentManager = fragmentManager;
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View v = layoutInflater.inflate(R.layout.completion_charges_layout, null);

        rv = v.findViewById(R.id.orderItemList);
        notify = v.findViewById(R.id.notify);
        whatsapp = v.findViewById(R.id.whatsapp);
        msg = v.findViewById(R.id.message);
        call = v.findViewById(R.id.call);
        notifyText = v.findViewById(R.id.textNotify);
        ll =v.findViewById(R.id.layoutNotifyHidden);
        amtPending = v.findViewById(R.id.amountPending);
        takePay = v.findViewById(R.id.payment);
        bill = v.findViewById(R.id.bill);
        back = v.findViewById(R.id.goBack);
        ahead = v.findViewById(R.id.goAhead);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        takePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentOptions paymentOptions = new PaymentOptions();
                paymentOptions.show(fragmentManager,paymentOptions.getTag());
            }
        });

        bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateBill();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        Glide.with(activity)
                .load(activity.getResources().getDrawable(R.drawable.whatsapp_logo))
                .apply(RequestOptions.circleCropTransform())
                .into(whatsapp);


        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(1000);
                notify.startAnimation(rotateAnimation);


                if (isExpanded) {
                    k=0;
                    animateButton(msg, 0, false);
                    animateButton(call, 200, false);
                    animateButton(whatsapp, 400, false);
                    isExpanded=false;
                } else {
                    notifyText.setVisibility(View.INVISIBLE);
                    animateButton(msg, 0, true);
                    animateButton(call, 200, true);
                    animateButton(whatsapp, 400, true);
                    isExpanded = true;
                }
            }
        });

        return v;
    }

    View view;
    ImageView sign;
    private void generateBill() {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv2.setLayoutManager(new LinearLayoutManager(getContext()));

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
        BillAdapter adapter = new BillAdapter(toBeSent, getContext(), activity);
        rv.setAdapter(adapter);

        Add_Item.RupeesConverter rupeesConverter = new Add_Item.RupeesConverter();
        String result = rupeesConverter.convertToIndianRupeesWords((int) total);

        words.setText("Rupees in words: " + result);
        for(Payment p: pays)
        {
            toBeSent2.add(new Pair<>("Payment", new Pair<>(p.getName(), "-\u20B9 " + p.getAmount())));
            total -= Integer.parseInt(p.getAmount());
        }

        BillAdapter adapter2 = new BillAdapter(toBeSent2, getContext(), activity);
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

                        Glide.with(activity)
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

        File pdfFile = new File(getContext().getFilesDir(), fileName + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Writing pdf document failed", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(getContext(), Bill_Signature.class);
        intent.putExtra("fileUri", Uri.fromFile(pdfFile));
        activity.startActivityForResult(intent, 143);
    }

    public void signBill(byte[] Signature)
    {
        pdfDocument = new PdfDocument();
        System.out.println(Signature + "*****************************");
        Glide.with(getContext()).as(byte[].class).load(Signature).addListener(new RequestListener<byte[]>() {
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
                File pdfFile = new File(getContext().getFilesDir(), fileName + ".pdf");

                try {
                    pdfDocument.writeTo(new FileOutputStream(pdfFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Writing PDF document failed", Toast.LENGTH_SHORT).show();
                }

                Uri pdfUri = FileProvider.getUriForFile(getContext(), "com.example.tailmate.fileprovider", pdfFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Handle the exception when a PDF viewer app is not installed
                }

                pdfDocument.close();
                return false;
            }
        }).submit();

    }
    int k = 0;
    private void animateButton(final View view, int delay, boolean b) {
        ObjectAnimator animator;
        if(b)
            animator = ObjectAnimator.ofFloat(view, "translationX", 1000f, 0f);
        else
            animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 1000f);
        animator.setDuration(500);
        animator.setStartDelay(delay);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!b)
                {
                    view.setVisibility(View.INVISIBLE);
                    if(++k==3)
                        notifyText.setVisibility(View.VISIBLE);
                }

            }
        });
        animator.start();
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

    private void CallTheNumber(String phoneNumber) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if it is not granted
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                return;
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                activity.startActivity(intent);
            }
        }
    }

    private void openMessagingAppWithNumber(String phoneNumber) {
        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", getMessage());

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "No messaging app found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String getStepData() {
        String str = null;
        if(OrderDetails.order != null)
            str = OrderDetails.order.getDates().get("Payment");
        return str;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return getStepData();
    }

    @Override
    protected void restoreStepData(String data) {

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        return null;
    }

    @Override
    protected void onStepOpened(boolean animated) {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //OrderDetails.verticalStepperForm.markOpenStepAsUncompleted(true, null);
                OrderDetails.verticalStepperForm.goToPreviousStep(true);
            }
        });

        ahead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,String> dates= OrderDetails.order.getDates();

                java.util.Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

                if(dates.get("Delivered")==null)
                {
                    dates.put("Delivered", dateFormat.format(currentDate));
                    OrderDetails.order.setDates(dates);
                    OrderDetails.order.setPending(true);
                    Map<String, Object> tobeupdated = new HashMap<>();
                    tobeupdated.put("Dates", dates);
                    tobeupdated.put("Status", "Delivered");
                    tobeupdated.put("Pending", true);
                    firebaseFirestore.collection("Orders").document(hash(firebaseAuth.getCurrentUser().getPhoneNumber()))
                            .collection("Details").document(OrderDetails.order.getOrderID()).update(tobeupdated);
                }

                OrderDetails.verticalStepperForm.markOpenStepAsCompleted(true);
                OrderDetails.verticalStepperForm.goToNextStep(true);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallTheNumber(OrderDetails.order.getCustomer().getMobileNumber());
            }
        });

        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMessagingAppWithNumber(OrderDetails.order.getCustomer().getMobileNumber());
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isUserWhatsAppInstalled = isWhatsAppInstalled();
                boolean doesNumberHaveWhatsApp = doesNumberHaveWhatsApp(OrderDetails.order.getCustomer().getMobileNumber());
                if (isUserWhatsAppInstalled && doesNumberHaveWhatsApp) {
                    sendWhatsAppMessage(OrderDetails.order.getCustomer().getMobileNumber());
                } else {
                    Toast.makeText(activity, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isWhatsAppInstalled() {
        PackageManager packageManager = activity.getPackageManager();
        try {
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(activity, "Whatsapp is not installed", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean doesNumberHaveWhatsApp(String phoneNumber) {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(phoneNumber)));
        intent.setPackage("com.whatsapp");
        return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    private void sendWhatsAppMessage(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + phoneNumber + "?text=" + urlEncodeString(getMessage())));
        activity.startActivity(intent);
    }

    private String getMessage()
    {
        Order order = OrderDetails.order;
        String customerItems = "";
        int i=0;
        for(OrderItem orderItem: OrderDetails.itemlistadaptor.getOrderItems())
        {
            if(i==OrderDetails.itemlistadaptor.getItemCount()-1)
                customerItems += orderItem.getName();
            else
                customerItems += orderItem.getName() + ", ";

            i++;
        }
        message_matter = "🎉 Hi " + order.getCustomer().getName() + "!\n\n Your order is now complete! 🎉\n\n"
                + "Order Items: " + customerItems + "\n"
                + "Order ID: " + order.getOrderID() + "\n"
                + "Total Amount: " + OrderDetails.totalAmt.getText().toString() + "\n"
                + "Amount Pending: " + amtPending.getText().toString() + "\n\n"
                + "Thank you for choosing our tailoring services! Your order has been beautifully crafted and is ready for pickup. " +
                "\n\nWe look forward to serving you again in the future. 😊🧵";


        return message_matter;
    }

    public String urlEncodeString(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
    @Override
    protected void onStepClosed(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }
}
