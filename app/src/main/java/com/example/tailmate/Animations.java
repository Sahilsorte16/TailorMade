package com.example.tailmate;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.CountDownLatch;

public class Animations {
    public static AnimatorSet goInAnimation(RecyclerView.ViewHolder holder)
    {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 1.0f, 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1.0f, 0.8f, 1.0f);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        return animatorSet;
    }

    public static AnimatorSet backAnimation(View v)
    {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.8f, 1.0f);
        scaleXAnimator.setDuration(150);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.8f, 1.0f);
        scaleYAnimator.setDuration(150);

        // Create an AnimatorSet and add the animators
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        return animatorSet;
    }
}
