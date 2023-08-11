package com.example.tailmate;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Pair<String,Integer>> ways;
    Context context;
    Order order;
    Activity activity;
    Fragment fragment;
    public PaymentAdapter(Order order, Context context, Activity activity, Fragment fragment)
    {
        ways = new ArrayList<>();
        ways.add(new Pair<>("Cash", R.drawable.cod));
        ways.add(new Pair<>("Scan To Pay", R.drawable.upi));
        this.context = context;
        this.activity = activity;
        this.fragment = fragment;
        this.order = order;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.pay_way, parent, false);
        Drawable background = ContextCompat.getDrawable(parent.getContext(), R.drawable.selector_ltgray);
        view.setBackground(background);
        return new PaymentAdapter.ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClassViewHolder classViewHolder = (ClassViewHolder) holder;
        Glide.with(context)
                .load(context.getDrawable(ways.get(position).second))
                .into(classViewHolder.iv);

        classViewHolder.tv.setText(ways.get(position).first);

        classViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimatorSet animatorSet = Animations.goInAnimation(holder);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        Intent intent = new Intent(context, QRDisplay.class);
                        intent.putExtra("Customer", order.getCustomer().getName());
                        intent.putExtra("Amount", order.getCharges());
                        intent.putExtra("Mode", ways.get(holder.getLayoutPosition()).first);
                        activity.startActivityForResult(intent, 45);
                        ((PaymentOptions)fragment).dismiss();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }
                });
                animatorSet.start();
            }
        });

    }
    @Override
    public int getItemCount() {
        return ways.size();
    }

    private class ClassViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;
        public ClassViewHolder(View view) {
            super(view);
            iv = view.findViewById(R.id.logo);
            tv = view.findViewById(R.id.text);
        }
    }
}
