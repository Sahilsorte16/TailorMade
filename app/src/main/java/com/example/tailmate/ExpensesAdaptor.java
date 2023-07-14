package com.example.tailmate;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpensesAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Pair<String,String>> exs;
    Context context;
    Activity activity;

    public ExpensesAdaptor(List<Pair<String,String>> map, Context context, Activity activity)
    {
        exs = map;
        this.context = context;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.expenses, parent, false);
        return new ExpensesAdaptor.DisplayCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayCardViewHolder displayCardViewHolder = (DisplayCardViewHolder) holder;
        displayCardViewHolder.bind(exs.get(position).first, exs.get(position).second);
    }

    @Override
    public int getItemCount() {
        return exs.size();
    }

    public void addExpense(String str1, String str2)
    {
        exs.add(new Pair<>(str1,str2));
        notifyDataSetChanged();
    }

    public List<Pair<String,String>> getExpenses()
    {
        return exs;
    }

    public void removeItem(int layoutPosition) {
        exs.remove(layoutPosition);
        notifyItemRemoved(layoutPosition);
    }

    public class DisplayCardViewHolder extends RecyclerView.ViewHolder {

        TextView eName, eVal;
        public DisplayCardViewHolder(View view) {
            super(view);
            eName = view.findViewById(R.id.exName);
            eVal = view.findViewById(R.id.exValue);
        }

        public void bind(String str1, String str2) {
            eName.setText(str1);
            eVal.setText("\u20B9 " + str2);
        }
    }
}
