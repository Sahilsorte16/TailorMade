package com.example.tailmate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

public class ImageAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Bitmap> ImageUrls;
    Activity activity;
    String imageType;
    public ImageAdaptor(List<Bitmap> imageUrls, Activity activity, String imageType)
    {
        ImageUrls = imageUrls;
        this.activity = activity;
        this.imageType = imageType;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.image_card, parent, false);
        return new ImageAdaptor.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Bitmap bitmap = ImageUrls.get(position);
        ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        imageViewHolder.bind(bitmap);
        ((ImageViewHolder) holder).tv.setText(imageType + String.valueOf(holder.getLayoutPosition()+1));
        ((ImageViewHolder) holder).del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getLayoutPosition();
                ImageUrls.remove(pos);
                notifyItemRemoved(pos);

                if(activity instanceof PrepareOrder)
                {
                    if(getItemCount()==0)
                    {
                        ((PrepareOrder) activity).aSwitch.setChecked(false);
                        ((PrepareOrder) activity).msg.setVisibility(View.VISIBLE);
                        ((PrepareOrder) activity).aSwitch.setVisibility(View.GONE);
                    }
                    else {
                        ((PrepareOrder) activity).msg.setVisibility(View.GONE);
                        ((PrepareOrder) activity).aSwitch.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ImageUrls.size();
    }

    public void addImage(Bitmap croppedBitmap) {
        ImageUrls.add(croppedBitmap);
        notifyDataSetChanged();
    }

    public List<Bitmap> getImageUrls(){
        return ImageUrls;
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView tv;
        public ImageView del, img;

        public ImageViewHolder(View view) {
            super(view);

            tv = view.findViewById(R.id.imageName);
            del = view.findViewById(R.id.delete);
            img = view.findViewById(R.id.image);
            if(activity instanceof OrderItemDetails)
                del.setVisibility(View.GONE);

        }

        public void bind(Bitmap bitmap) {
            Glide.with(activity)
                    .asBitmap()
                    .load(bitmap)
                    .into(new BitmapImageViewTarget(img) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            // Image loaded successfully
                        }
                    });
        }
    }
}
