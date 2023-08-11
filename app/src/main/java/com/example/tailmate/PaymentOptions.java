package com.example.tailmate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PaymentOptions extends BottomSheetDialogFragment {

    RecyclerView rv1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv1 = view.findViewById(R.id.paymentMethod);
        rv1.setLayoutManager(new LinearLayoutManager(getContext()));
        PaymentAdapter paymentAdapter = new PaymentAdapter(OrderDetails.order, getContext(), getActivity(), PaymentOptions.this);
        rv1.setAdapter(paymentAdapter);
    }
}