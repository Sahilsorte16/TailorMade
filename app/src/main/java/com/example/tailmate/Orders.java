package com.example.tailmate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;


public class Orders extends Fragment {

    TabLayout tabLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_orders, container, false);

        tabLayout = v.findViewById(R.id.tabs);
        int defaultTabPosition=1;
        TabLayout.Tab defaultTab = tabLayout.getTabAt(defaultTabPosition);
        if (defaultTab != null) {
            defaultTab.select();
            tabLayout.setScrollPosition(defaultTabPosition, 0f, true);
        }

        return v;
    }
}