package com.example.tailmate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QRDisplay extends AppCompatActivity {

    ImageView qr, logo;
    EditText amount;
    TextView customer, merchant, vpaId;
    Button received;
    CheckBox cb;
    private String nameOfMerchant;
    private String upiId;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  //      getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);;
        setContentView(R.layout.activity_qrdisplay);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        qr = findViewById(R.id.qr);
        amount = findViewById(R.id.amount);
        customer = findViewById(R.id.customer_name);
        merchant = findViewById(R.id.merchant);
        vpaId = findViewById(R.id.vpa);
        logo = findViewById(R.id.upi_logo);
        received = findViewById(R.id.received);
        cb = findViewById(R.id.checkBox);


        nameOfMerchant = "Kishori Sorte";
        upiId = "9860882505@icici";

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent in = getIntent();
        String mode = in.getStringExtra("Mode");
        amount.setText(in.getStringExtra("Amount"));
        customer.setText("from " + in.getStringExtra("Customer"));
        merchant.setText("Merchant Name: " + nameOfMerchant);
        vpaId.setText("VPA ID: " + upiId);
        cb.setText("I have received the payment of \u20B9 " + amount.getText());

        if(Integer.parseInt(amount.getText().toString())<=0)
        {
            Toast.makeText(QRDisplay.this, "No amount pending", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        if(mode.equals("Cash"))
        {
            qr.setVisibility(View.GONE);
            logo.setVisibility(View.GONE);
            merchant.setVisibility(View.GONE);
            vpaId.setVisibility(View.GONE);
        }
        else
        {
            initViews();
        }
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(amount.getText().length()==0)
                {
                    cb.setText("I have received the payment of \u20B9 0");
                }
                else
                {
                    cb.setText("I have received the payment of \u20B9 " + amount.getText());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                received.setEnabled(b);
            }
        });
        received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Integer.parseInt(amount.getText().toString())>0)
                {
                    Intent intent = new Intent();
                    intent.putExtra("Amount", amount.getText().toString());
                    intent.putExtra("MOP", "UPI");
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    Toast.makeText(QRDisplay.this, "Check your amount", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onTouchEvent(event);
    }


    private void initViews() {
        String url = "upi://pay?pa=" + // payment method.
                upiId +         // VPA number.
                "&am=" + Integer.parseInt(amount.getText().toString()) +       // this param is for fixed amount (non-editable).
                "&pn=" + urlEncodeString(nameOfMerchant) +     // to show your name in the app.
                "&cu=INR" +                  // Currency code.
                "&mode=02" ;                // mode O2 for Secure QR Code.;

        Bitmap bitmap = textToImageEncode(url);
        Glide.with(getApplicationContext())
                .load(bitmap)
                .into(qr);
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

    private Bitmap textToImageEncode(String value) {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new QRCodeWriter().encode(
                    value,
                    BarcodeFormat.QR_CODE,
                    500,
                    500,
                    null
            );
        } catch (WriterException e) {
            Log.e("QR Code Writer", e.getMessage());
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.parseColor("#000000") : Color.parseColor("#ffffff");
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
}