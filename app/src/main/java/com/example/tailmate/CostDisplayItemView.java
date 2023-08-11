package com.example.tailmate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CostDisplayItemView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<OrderItem> orderItems;
    List<Payment> payments;
    TextView Cost;
    Context context;
    Activity activity;
    ExpensesAdaptor paymentexpensesAdaptor;
    long amt=0;

    public CostDisplayItemView(List<OrderItem> orderItems, List<Payment> payments, Context context, Activity activity)
    {
        this.orderItems = orderItems;
        this.payments = payments;
        this.context = context;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.payment_order_item, parent, false);
        return new CostDisplayItemView.CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getLayoutPosition() == getItemCount()-1)
        {
            ((CardViewHolder) holder).bindPaymentHolder(holder.getLayoutPosition());
        }
        else
        {
            OrderItem orderItem = orderItems.get(position);
            ((CardViewHolder) holder).bind(orderItem, holder.getLayoutPosition());
        }

        ((CardViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView rv = ((CardViewHolder) holder).rv;
                ImageView iv = ((CardViewHolder) holder).down;
                if(rv.getVisibility()==View.VISIBLE)
                {
                    //rv.setVisibility(View.GONE);
                    animateRecyclerViewVisibility(rv, View.GONE);
                    iv.setImageResource(R.drawable.baseline_arrow_drop_down_24);
                }
                else
                {
                    //rv.setVisibility(View.VISIBLE);
                    animateRecyclerViewVisibility(rv, View.VISIBLE);
                    iv.setImageResource(R.drawable.baseline_arrow_drop_up_24);
                }

            }
        });
    }

    public static void animateRecyclerViewVisibility(final RecyclerView recyclerView, final int visibility) {
        if (recyclerView.getVisibility() == visibility) {
            // No need to animate if the current visibility is already the desired visibility
            return;
        }

        float startAlpha = (visibility == View.VISIBLE) ? 0.0f : 1.0f;
        float endAlpha = (visibility == View.VISIBLE) ? 1.0f : 0.0f;

        recyclerView.setAlpha(startAlpha);
        recyclerView.setVisibility(View.VISIBLE);

        recyclerView.animate()
                .alpha(endAlpha)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        recyclerView.setVisibility(visibility);
                    }
                })
                .start();
    }

    public String addToPaidAmount(Payment pay)
    {
        pay.setName("Installment " + String.valueOf(payments.size()+1));
        payments.add(pay);
        paymentexpensesAdaptor.addExpense(pay.getName(), pay.getAmount());
        long amt=0;
        for(Payment p: payments)
        {
            amt += Integer.parseInt(p.getAmount());
        }
        Cost.setText("- \u20B9 " + String.valueOf(amt));
        return makeAmountOf();
    }

    public List<Payment> getPaymentList()
    {
        return new ArrayList<>(payments);
    }

    @Override
    public int getItemCount() {
        return orderItems.size()+1;
    }

    public String makeAmountOf()
    {
        long amt=0;
        for(int i=0; i< orderItems.size(); i++)
        {
            int o = Integer.parseInt(orderItems.get(i).getTotalItemCharges());
            amt += o;
        }

        for(Payment p: payments)
        {
            amt -= Integer.parseInt(p.getAmount());
        }
        return String.valueOf(amt);
    }

    public void setUpnewPays(List<Payment> paymentList) {
        payments = paymentList;
        notifyDataSetChanged();
    }

    private class CardViewHolder extends RecyclerView.ViewHolder {

        TextView itemName, cost, qty;
        ImageView down;
        RecyclerView rv;
        public CardViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.itemName);
            cost = view.findViewById(R.id.cost);
            rv = view.findViewById(R.id.expenses);
            qty = view.findViewById(R.id.qty);
            down = view.findViewById(R.id.dropdown);
            rv.setLayoutManager(new LinearLayoutManager(context));
            Cost = cost;
        }

        public void bind(OrderItem orderItem, int pos) {
            itemName.setText(orderItem.getName());
            ExpensesAdaptor expensesAdaptor = new ExpensesAdaptor(orderItem.getExpenses(),context,activity);
            //expensesAdaptor.addExpense(orderItem.getType() + " Charges", orderItem.getCharges());
            qty.setText(orderItem.getQuantity()+ " X");
            long ans = expensesAdaptor.makeAmount();
            cost.setText("\u20B9 "+ String.valueOf(ans));
            rv.setAdapter(expensesAdaptor);
        }

        public void bindPaymentHolder(int layoutPosition) {
            List<Pair<String,String>> expenses = new ArrayList<>();
            for(Payment p: payments)
            {
                expenses.add(new Pair<>(p.getName(), p.getAmount()));
            }
            paymentexpensesAdaptor = new ExpensesAdaptor(expenses, context, activity);

            long ans = paymentexpensesAdaptor.makeAmount();
            itemName.setText("Paid Amount");
            qty.setVisibility(View.GONE);
            cost.setText("- \u20B9 "+ String.valueOf(ans));
            cost.setTextColor(activity.getColor(R.color.Delete_red));
            rv.setAdapter(paymentexpensesAdaptor);
        }
    }
}
