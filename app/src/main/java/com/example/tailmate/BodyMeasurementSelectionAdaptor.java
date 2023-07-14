package com.example.tailmate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BodyMeasurementSelectionAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<MeasureCardItem> bm;
    Context context;

    public BodyMeasurementSelectionAdaptor(List<MeasureCardItem> bm, Context context)
    {
        this.bm = bm;
        this.context = context;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.choose_measurement, parent, false);
        return new BodyMeasurementSelectionAdaptor.DisplayCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MeasureCardItem mci = bm.get(position);
        DisplayCardViewHolder dvh = ((DisplayCardViewHolder) holder);
        dvh.bind(mci);
        setBackground(dvh,mci);
        dvh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mci.getEt().getText().length()==0)
                {
                    Toast.makeText(context, "The value of " + mci.getTitle() + " is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mci.setSelected(!mci.isSelected());
                setBackground(dvh, mci);
            }
        });
    }

    private void setBackground(DisplayCardViewHolder dvh, MeasureCardItem mci) {
        Drawable bg;
        if(mci.isSelected())
            bg = context.getResources().getDrawable(R.drawable.rectangle_grey);
        else
            bg = context.getResources().getDrawable(R.drawable.rectangle);

        dvh.itemView.setBackground(bg);
    }

    public List<MeasureCardItem> getMeasurementList() {
        for(int i=0; i<bm.size(); i++)
        {
            //System.out.println(bm.get(i).getEt().getText().toString());
            bm.get(i).setLength(bm.get(i).getEt().getText().toString());
        }

        return bm;
    }

    @Override
    public int getItemCount() {
        return bm.size();
    }

    public void addItem(MeasureCardItem cardItem) {
        bm.add(cardItem);
        notifyItemInserted(bm.size() - 1);
    }

    private class DisplayCardViewHolder extends RecyclerView.ViewHolder {
        TextView bmt;
        public EditText len;
        ImageView imv;
        public DisplayCardViewHolder(View view) {
            super(view);
            bmt = view.findViewById(R.id.bmt);
            len = view.findViewById(R.id.len);
            imv = view.findViewById(R.id.imv);
        }

        public void bind(MeasureCardItem cardItem) {
            bmt.setText(cardItem.getTitle());
            cardItem.setEt(len);
            Uri uri = cardItem.getImageUri();
            if(uri==null)
            {
                imv.setImageResource(cardItem.getImageResId());
            }
            else
            {
                Glide.with(context.getApplicationContext())
                        .load(uri)
                        .into(imv);
            }
            len.setText(cardItem.getLength());
        }
    }
}
