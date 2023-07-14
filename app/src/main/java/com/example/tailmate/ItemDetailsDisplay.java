package com.example.tailmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDetailsDisplay extends Fragment {

    TextView name, type, totalCharges;
    RecyclerView bms, instructions, clothes, patterns, dresses, charges;
    List<String> Instructions;
    List<Bitmap> ClothImages, PatternImages, DressImages;
    Map<String,String> bodyMs;
    List<Pair<String,String>> expenses;
    ExpensesAdaptor expensesAdaptor;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage storage;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_item_details_display, container, false);

        name = v.findViewById(R.id.itemName);
        type = v.findViewById(R.id.itemType);
        bms = v.findViewById(R.id.measurements);
        instructions = v.findViewById(R.id.instructions);
        clothes = v.findViewById(R.id.clothes);
        patterns = v.findViewById(R.id.patterns);
        dresses = v.findViewById(R.id.dresses);
        charges = v.findViewById(R.id.Charges);
        totalCharges = v.findViewById(R.id.totalCharges);

        bms.setLayoutManager(new LinearLayoutManager(getContext()));
        instructions.setLayoutManager(new LinearLayoutManager(getContext()));
        charges.setLayoutManager(new LinearLayoutManager(getContext()));
        clothes.setLayoutManager(new GridLayoutManager(getContext(), 2));
        patterns.setLayoutManager(new GridLayoutManager(getContext(), 2));
        dresses.setLayoutManager(new GridLayoutManager(getContext(), 2));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Instructions = new ArrayList<>();
        ClothImages = new ArrayList<>();
        PatternImages = new ArrayList<>();
        DressImages = new ArrayList<>();
        expenses = new ArrayList<>();
        bodyMs = new HashMap<>();

        Intent in = getActivity().getIntent();
        name.setText(in.getStringExtra("Item Name"));
        type.setText(in.getStringExtra("Item Type"));
        Instructions = in.getStringArrayListExtra("Instructions");

        ClothImages =  byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Cloth Images"));
        PatternImages = byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Pattern Images"));
        DressImages = byteToBitmap((ArrayList<byte[]>) in.getSerializableExtra("Dress Images"));

        bodyMs = (Map<String, String>) in.getSerializableExtra("Body Measurements");

        Gson gson = new Gson();
        String json = in.getStringExtra("Expenses");
        Type listType = new TypeToken<List<Pair<String,String>>>() {}.getType();
        expenses = gson.fromJson(json, listType);
        expenses.add(new Pair<>(type.getText().toString() + " Charges", in.getStringExtra("Charges")));

        expensesAdaptor = new ExpensesAdaptor(expenses, getContext(), getActivity());
        charges.setAdapter(expensesAdaptor);

        long ttl=0;
        for(Pair<String,String> p: expensesAdaptor.getExpenses())
        {
            ttl += Integer.parseInt(p.second);
        }
        totalCharges.setText("\u20B9 " + String.valueOf(ttl));

        InstructionsAdapter instructionsAdapter = new InstructionsAdapter(Instructions, getActivity());
        instructions.setAdapter(instructionsAdapter);

        ImageAdaptor imageAdaptor = new ImageAdaptor(ClothImages, getActivity(), "Cloth ");
        clothes.setAdapter(imageAdaptor);

        ImageAdaptor imageAdaptor1 = new ImageAdaptor(PatternImages, getActivity(), "Pattern ");
        patterns.setAdapter(imageAdaptor1);

        ImageAdaptor imageAdaptor2 = new ImageAdaptor(DressImages, getActivity(), "Dress ");
        dresses.setAdapter(imageAdaptor2);

        setUpBodyMs();
        return v;
    }

    private void setUpBodyMs()
    {
        List<MeasureCardItem> list = new ArrayList<>();
        Map<String, Uri> mp = new HashMap<>();
        showLoadingDialog();
        StorageReference storageRef = storage.getReference().child("Body Measurements");
        storageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            String imageName = item.getName().replace(".jpg", "");
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mp.put(imageName, uri);
                                    if(mp.size()==listResult.getItems().size())
                                    {
                                        for(String key: bodyMs.keySet())
                                        {
                                            MeasureCardItem mci = new MeasureCardItem(key, bodyMs.get(key));
                                            if(mp.containsKey(key))
                                            {
                                                mci.setImageUri(mp.get(key));
                                            }
                                            list.add(mci);
                                        }

                                        MeasureCardAdapter measureCardAdapter = new MeasureCardAdapter(list, getContext(), false);
                                        bms.setAdapter(measureCardAdapter);
                                        dismissLoadingDialog();
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private List<Bitmap> byteToBitmap(ArrayList<byte[]> imageBytesList) {
        List<Bitmap> bitmapList = new ArrayList<>();

        for (byte[] imageBytes : imageBytesList) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            bitmapList.add(bitmap);
        }

        return bitmapList;
    }

    private ProgressDialog progressDialog;

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}