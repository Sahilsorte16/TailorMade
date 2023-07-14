package com.example.tailmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PrepareOrder extends AppCompatActivity {

    TextView iName, iType, stitchCharges, totalCharges, msg, stitchName;
    RecyclerView recyclerView, recyclerView1;
    ExpensesAdaptor expensesAdaptor;
    ImageView addInstr, addDress, back;
    Switch aSwitch;
    long pay=0;
    List<Pair<String,String>> expenses;
    List<Bitmap> dresses;
    private int CAMERA_PERMISSION_REQUEST_CODE = 109;
    private int CAMERA_REQUEST_CODE = 110;
    private ImageAdaptor imageAdaptor;
    private int pos;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_order);

        iName = findViewById(R.id.itemName);
        iType = findViewById(R.id.itemType);
        back = findViewById(R.id.back);
        stitchName = findViewById(R.id.stitchName);
        stitchCharges = findViewById(R.id.stitchCharges);
        totalCharges = findViewById(R.id.totalCharges);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        addInstr = findViewById(R.id.addInstr);
        addDress = findViewById(R.id.addDress);
        msg = findViewById(R.id.msg);
        aSwitch = findViewById(R.id.switchComplete);

        expenses = new ArrayList<>();
        dresses = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView1.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));

        Intent in = getIntent();
        iName.setText(in.getStringExtra("Item Name"));
        iType.setText(in.getStringExtra("Item Type"));
        pos = in.getIntExtra("LayoutPosition", -1);
        id = in.getStringExtra("Id");
        stitchName.setText(iType.getText().toString() + " Charges");
        stitchCharges.setText("\u20B9 " + in.getStringExtra("Charges"));
        aSwitch.setChecked(in.getBooleanExtra("Complete", false));
        Gson gson = new Gson();
        String json = in.getStringExtra("Expenses");
        Type listType = new TypeToken<List<Pair<String, String>>>() {}.getType();
        expenses = gson.fromJson(json, listType);
        dresses = byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Dress Images"));

        expensesAdaptor = new ExpensesAdaptor(expenses, getApplicationContext(),PrepareOrder.this);
        recyclerView.setAdapter(expensesAdaptor);

        imageAdaptor = new ImageAdaptor(dresses, PrepareOrder.this, "Dress ");
        recyclerView1.setAdapter(imageAdaptor);

        pay = Integer.parseInt(in.getStringExtra("Charges"));
        totalCharges.setText("\u20B9 " + String.valueOf(pay+totalPayment()));

        if(imageAdaptor.getItemCount()==0)
        {
            aSwitch.setChecked(false);
            msg.setVisibility(View.VISIBLE);
            aSwitch.setVisibility(View.GONE);
        }
        else {
            msg.setVisibility(View.GONE);
            aSwitch.setVisibility(View.VISIBLE);
        }
        addInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCardDialog();
            }
        });
        addDress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPicture();
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

    private void implementSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                expensesAdaptor.removeItem(viewHolder.getLayoutPosition());
                totalCharges.setText("\u20B9 " + String.valueOf(pay+totalPayment()));
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(PrepareOrder.this, R.color.Delete_red))
                        .addSwipeLeftActionIcon(R.drawable.baseline_delete_24)
                        .addSwipeLeftLabel("Delete Expense")
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public long totalPayment() {
        List<Pair<String,String>> list = expensesAdaptor.getExpenses();
        long amt = 0;

        if(list==null)
            return amt;

        for(Pair<String,String> p: list)
        {
            amt += Integer.parseInt(p.second);
        }
        return amt;
    }

    private void clickPicture() {
        if (ContextCompat.checkSelfPermission(PrepareOrder.this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(PrepareOrder.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if(imageBitmap != null)
                {
                    Bitmap croppedBitmap = cropToSquare(imageBitmap);
                    imageAdaptor.addImage(croppedBitmap);
                    if(imageAdaptor.getItemCount()==0)
                    {
                        aSwitch.setChecked(false);
                        msg.setVisibility(View.VISIBLE);
                        aSwitch.setVisibility(View.GONE);
                    }
                    else {
                        msg.setVisibility(View.GONE);
                        aSwitch.setVisibility(View.VISIBLE);
                    }
                }

            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        return Bitmap.createBitmap(bitmap, x, y, size, size);
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrepareOrder.this);
        LayoutInflater inflater = LayoutInflater.from(PrepareOrder.this);
        View dialogView = inflater.inflate(R.layout.add_measurement_dialog, null);
        final EditText editTextParameter1 = dialogView.findViewById(R.id.editTextParameter1);
        final EditText editTextParameter2 = dialogView.findViewById(R.id.editTextParameter2);
        editTextParameter1.setHint("Expense Name");
        editTextParameter2.setHint("Expense Value (in Rupees)");
        builder.setView(dialogView)
                .setTitle("Add New Measurement")
                .setIcon(R.drawable.baseline_add_24)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String parameter1 = editTextParameter1.getText().toString().trim();
                        String parameter2 = editTextParameter2.getText().toString().trim();

                        if (!parameter1.isEmpty() && !parameter2.isEmpty()) {
                            expensesAdaptor.addExpense(parameter1, parameter2);
                            totalCharges.setText("\u20B9 " + String.valueOf(pay+totalPayment()));
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private List<Bitmap> byteToBitmap(ArrayList<byte[]> imageBytesList) {
        List<Bitmap> bitmapList = new ArrayList<>();

        for (byte[] imageBytes : imageBytesList) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            bitmapList.add(bitmap);
        }

        return bitmapList;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        //intent.putExtra("Expenses", (Serializable) expensesAdaptor.getExpenses());
        Gson gson = new Gson();
        String json = gson.toJson(expensesAdaptor.getExpenses());
        intent.putExtra("Expenses",  json);
        intent.putExtra("Dresses", convertToByteArray(imageAdaptor.getImageUrls()));
        intent.putExtra("Total Charges", String.valueOf(pay + totalPayment()));
        intent.putExtra("isCompleted", aSwitch.isChecked());
        intent.putExtra("Id", id);
        intent.putExtra("Position", pos);
        setResult(RESULT_OK, intent);
        finish();
    }

    private ArrayList<byte[]> convertToByteArray(List<Bitmap> bitmapList) {
        ArrayList<byte[]> byteArrayArrayList = new ArrayList<>();
        for (Bitmap bitmap : bitmapList) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            byteArrayArrayList.add(byteArray);
        }
        return byteArrayArrayList;
    }
}