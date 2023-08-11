package com.example.tailmate;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExpensesAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Pair<String,String>> exs;
    Context context;
    Activity activity;

    public ExpensesAdaptor(List<Pair<String,String>> expenses, Context context, Activity activity)
    {
        exs = expenses;
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
        if(getItemCount()>0)
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

    public long makeAmount()
    {
        long amt=0;
        for(Pair<String,String> p: exs)
        {
            amt += Integer.parseInt(p.second);
        }

        return amt;
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
