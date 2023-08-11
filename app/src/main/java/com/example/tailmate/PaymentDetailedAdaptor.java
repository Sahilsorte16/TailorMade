package com.example.tailmate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaymentDetailedAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    List<Payment> pays;
    Context context;
    Activity activity;
    public PaymentDetailedAdaptor(List<Payment> pays, Context context, Activity activity)
    {
        this.pays = pays;
        this.context = context;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.payment_detailed_card, null, false);
        return new PaymentDetailedAdaptor.CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Payment p = pays.get(position);
        CardViewHolder cardViewHolder = (CardViewHolder) holder;
        cardViewHolder.bind(p);
    }

    @Override
    public int getItemCount() {
        return pays.size();
    }

    public void addPayment(Payment payment)
    {
        payment.setName("Installment " + String.valueOf(pays.size()+1));
        pays.add(payment);
        notifyDataSetChanged();
    }
    public long getTotalPaid() {
        long t = 0;
        for(Payment p: pays)
        {
            t += Integer.parseInt(p.getAmount());
        }
        return t;
    }

    public void removeItem(int layoutPosition) {
        pays.remove(layoutPosition);
        notifyItemRemoved(layoutPosition);
    }

    private class CardViewHolder extends RecyclerView.ViewHolder {

        TextView name, mode, dateDayTime, amount;
        public CardViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.installment);
            mode = view.findViewById(R.id.Mode);
            dateDayTime = view.findViewById(R.id.dateDayTime);
            amount = view.findViewById(R.id.amount);
        }

        public void bind(Payment payment)
        {
            name.setText(payment.getName());
            dateDayTime.setText(payment.getDate() + ", " + payment.getDay()+ ", " + payment.getTime());
            amount.setText("\u20b9 " + payment.getAmount());
            mode.setText(payment.getMop());
        }
    }
}
