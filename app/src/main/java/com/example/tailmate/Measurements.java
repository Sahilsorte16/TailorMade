package com.example.tailmate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;


public class Measurements extends Fragment {

    private RecyclerView recyclerView;
    private MeasureCardAdapter cardAdapter;
    private ActionMode actionMode;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_measurements, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        // Prepare sample data for the cards
        List<MeasureCardItem> cardItems = new ArrayList<>();
        cardItems.add(new MeasureCardItem(getString(R.string.full_height),R.drawable.full_height_f));
        cardItems.add(new MeasureCardItem(getString(R.string.waist_to_floor), R.drawable.waist_to_floor_f));
        cardItems.add(new MeasureCardItem(getString(R.string.waist_to_knee), R.drawable.waist_to_knee_f));
        cardItems.add(new MeasureCardItem(getString(R.string.waist_to_hip), R.drawable.waist_to_hip_f));
        cardItems.add(new MeasureCardItem(getString(R.string.Shoulder_to_Waist), R.drawable.shoulder_to_waist_f));
        cardItems.add(new MeasureCardItem(getString(R.string.Shoulder), R.drawable.shoulder_f));
        cardItems.add(new MeasureCardItem(getString(R.string.Bust_height), R.drawable.bust_height_f));
        cardItems.add(new MeasureCardItem(getString(R.string.Croch_to_Knee), R.drawable.croch_to_knee_f));
        cardItems.add(new MeasureCardItem(getString(R.string.leg_length),R.drawable.leg_length_f));
        cardItems.add(new MeasureCardItem(getString(R.string.body_rise), R.drawable.body_rise_f));
        cardItems.add(new MeasureCardItem(getString(R.string.Head), R.drawable.head_f));
        cardItems.add(new MeasureCardItem(getString(R.string.neck_size), R.drawable.neck_size_f));
        cardItems.add(new MeasureCardItem(getString(R.string.Arm_length), R.drawable.arm_length_f));
        // Add more card items as needed

        boolean editable = true;
        if(getActivity() instanceof BodyMeasurement)
        {
            editable = false;
        }
        cardAdapter = new MeasureCardAdapter(cardItems, getContext(), editable);
        recyclerView.setAdapter(cardAdapter);
        return v;
    }
}