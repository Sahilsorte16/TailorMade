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

public class BillAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_HEAD = 0;
    private static final int ITEM_CHARGES = 1;
    private static final int TOTAL = 2;
    private static final int PAYMENT = 3;
    private static final int SUB_TOTAL = 4;
    List<Pair<String,Object>> listOfObjects;
    Context context;
    Activity activity;
    public BillAdapter(List<Pair<String,Object>> listOfObjects, Context context, Activity activity)
    {
        this.activity = activity;
        this.context = context;
        this.listOfObjects = listOfObjects;
    }
    @Override
    public int getItemViewType(int position) {
        String str = listOfObjects.get(position).first;
        if(str.equals("Item Head"))
            return ITEM_HEAD;
        else if(str.equals("Charges"))
            return ITEM_CHARGES;
        else if(str.equals("Total"))
            return TOTAL;
        else if(str.equals("Payment"))
            return PAYMENT;
        else if(str.equals("Sub Total"))
            return SUB_TOTAL;
        else return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if(viewType==ITEM_HEAD)
        {
            View view = layoutInflater.inflate(R.layout.bill_item_head, null);
            return new BillAdapter.HeadViewer(view);
        }
        else if(viewType==ITEM_CHARGES)
        {
            View view = layoutInflater.inflate(R.layout.bill_charges, null);
            return new BillAdapter.ChargeViewer(view);
        }
        else if (viewType == SUB_TOTAL || viewType ==TOTAL)
        {
            View view = layoutInflater.inflate(R.layout.bill_totals, null);
            return new BillAdapter.TotalViewer(view);
        }
        else if (viewType == PAYMENT)
        {
            View view = layoutInflater.inflate(R.layout.bill_payment, null);
            return new BillAdapter.TotalViewer(view);
        }

        throw new RuntimeException("Sorry");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Object o = listOfObjects.get(position).second;
        if(viewType==ITEM_HEAD)
        {
            String name = (String) o;
            ((HeadViewer)holder).tv.setText(name);
        }
        else if(viewType==ITEM_CHARGES)
        {
            Pair<String,String> p = (Pair<String,String>) o;
            ((ChargeViewer)holder).tv1.setText(p.first);
            ((ChargeViewer)holder).tv2.setText(p.second);
        }
        else if(viewType == TOTAL)
        {
            Pair<String,String> p = (Pair<String,String>) o;
            ((TotalViewer)holder).tv1.setText(p.first);
            ((TotalViewer)holder).tv2.setText(p.second);
            ((TotalViewer)holder).tv3.setText("");
        }
        else if(viewType == PAYMENT)
        {
            Pair<String,String> p = (Pair<String,String>) o;
            ((TotalViewer)holder).tv1.setText(p.first);
            ((TotalViewer)holder).tv2.setText(p.second);
        }
        else if(viewType == SUB_TOTAL)
        {
            ArrayList<String> arr = (ArrayList<String>) o;
            ((TotalViewer)holder).tv1.setText(arr.get(0));
            ((TotalViewer)holder).tv2.setText(arr.get(1));
            ((TotalViewer)holder).tv3.setText(arr.get(2));
        }
    }

    @Override
    public int getItemCount() {
        return listOfObjects.size();
    }

    private class HeadViewer extends RecyclerView.ViewHolder {
        TextView tv;
        public HeadViewer(View view) {
            super(view);
            tv = view.findViewById(R.id.itemName);
        }
    }

    private class ChargeViewer extends RecyclerView.ViewHolder {
        TextView tv1,tv2;
        public ChargeViewer(View view) {
            super(view);
            tv1 = view.findViewById(R.id.charge);
            tv2 = view.findViewById(R.id.rupees);
        }
    }

    private class TotalViewer extends RecyclerView.ViewHolder {
        TextView tv1,tv2,tv3;
        public TotalViewer(View view) {
            super(view);
            tv1 = view.findViewById(R.id.totalText);
            tv2 = view.findViewById(R.id.amount);
            tv3 = view.findViewById(R.id.qty);
        }
    }
}
