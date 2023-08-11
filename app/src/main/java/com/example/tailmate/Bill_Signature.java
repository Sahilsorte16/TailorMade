package com.example.tailmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Bill_Signature extends AppCompatActivity {

    ImageView back, bill;
    SignaturePad signaturePad;
    Button submit, clear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_signature);

        back = findViewById(R.id.back);
        bill = findViewById(R.id.bill);
        clear = findViewById(R.id.clear);
        submit = findViewById(R.id.submit);

        signaturePad =findViewById(R.id.signaturePad);

        Intent in = getIntent();
        Uri fileUri = in.getParcelableExtra("fileUri");
        File file = new File(fileUri.getPath());

        try {
            Glide.with(getApplicationContext())
                    .load(convertPageToBitmap(file,0))
                    .into(bill);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signaturePad.clear();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap imageBitmap = signaturePad.getSignatureBitmap();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] imageBytes = outputStream.toByteArray();

                Intent intent = new Intent();
                intent.putExtra("Sign", imageBytes);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public static Bitmap convertPageToBitmap(File pdfFile, int pageIndex) throws IOException {
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

        PdfRenderer.Page pdfPage = pdfRenderer.openPage(pageIndex);

        Bitmap bitmap = Bitmap.createBitmap(pdfPage.getWidth(), pdfPage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        //canvas.drawColor(0xFFFFFFFF); // Set the canvas background color if needed
        pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        pdfPage.close();
        pdfRenderer.close();
        fileDescriptor.close();

        return bitmap;
    }

    public void generatePDF(File pdfFile)
    {
        Uri pdfUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.tailmate.fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle the exception when a PDF viewer app is not installed
        }
    }
}