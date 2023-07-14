package com.example.tailmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MeasureCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_ADD_CARD = 1;
    private static final int VIEW_DISPLAY_CUSTOMER_CARD = 2;
    private final Context context;
    private List<MeasureCardItem> cardItems;
    List<EditText> ets;
    boolean editable;

    public MeasureCardAdapter(List<MeasureCardItem> cardItems, Context context, boolean editable) {
        this.cardItems = cardItems;
        this.context = context;
        this.editable = editable;
        ets = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        // Return the appropriate view type based on the position
        if(editable)
        {
            if (position < cardItems.size()) {
                return VIEW_TYPE_CARD;
            } else {
                return VIEW_TYPE_ADD_CARD;
            }
        }
        else
            return VIEW_DISPLAY_CUSTOMER_CARD;

    }

    public void addItem(MeasureCardItem cardItem) {
        cardItems.add(cardItem);
        notifyItemInserted(cardItems.size() - 1);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (editable && viewType == VIEW_TYPE_CARD) {
            View view = inflater.inflate(R.layout.measurement_card, parent, false);
            return new CardViewHolder(view);
        } else if (editable && viewType == VIEW_TYPE_ADD_CARD) {
            View view = inflater.inflate(R.layout.add_card, parent, false);
            return new AddCardViewHolder(view);
        }
        else if(viewType == VIEW_DISPLAY_CUSTOMER_CARD)
        {
            View view = inflater.inflate(R.layout.display_body_card, parent,false);
            return new DisplayCardViewHolder(view);
        }

        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardViewHolder) {
            MeasureCardItem cardItem = cardItems.get(position);
            ((CardViewHolder) holder).bind(cardItem,position);
            ets.add(((CardViewHolder) holder).getEditText());
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(cardItem.isRemovable())
                            showDeleteCardDialog(cardItem, holder.getAdapterPosition());
                        else
                            Toast.makeText(context, "Default Measurements can't be deleted", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
        } else if (holder instanceof AddCardViewHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddCardDialog();
                }
            });
        } else if (holder instanceof DisplayCardViewHolder) {
            MeasureCardItem cardItem = cardItems.get(position);
            ((DisplayCardViewHolder) holder).bind(cardItem);
        }
    }

    private void showDeleteCardDialog(MeasureCardItem cardItem, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Measurement")
                .setIcon(R.drawable.baseline_delete_24)
                .setMessage("Do you want to delete '" + cardItem.getTitle() + "' ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cardItems.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public int getItemCount() {
        if(editable)
            return cardItems.size()+1;
        else
            return cardItems.size();
    }

    private static String hash(String phoneNumber) {
        String hash = phoneNumber;

        try {
            // Create an instance of the MD5 hashing algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convert the phone number to bytes and generate the hash
            md.update(phoneNumber.getBytes());
            byte[] digest = md.digest();

            // Convert the byte array to a BigInteger
            BigInteger bigInt = new BigInteger(1, digest);

            // Convert the BigInteger to a hexadecimal string
            hash = bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Handle exceptions related to the hashing algorithm
            e.printStackTrace();
        }

        return hash;
    }

    private void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.add_measurement_dialog, null);
        final EditText editTextParameter1 = dialogView.findViewById(R.id.editTextParameter1);
        final EditText editTextParameter2 = dialogView.findViewById(R.id.editTextParameter2);

        builder.setView(dialogView)
                .setTitle("Add New Measurement")
                .setIcon(R.drawable.baseline_add_24)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String parameter1 = editTextParameter1.getText().toString().trim();
                        String parameter2 = editTextParameter2.getText().toString().trim();

                        // Validate the input here and add the new card if valid
                        if (!parameter1.isEmpty() && !parameter2.isEmpty()) {
                            // Create a new CardItem and add it to the list
                            MeasureCardItem newCard = new MeasureCardItem(parameter1, parameter2);
                            newCard.setRemovable(true);
                            addItem(newCard);
                            //notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public List<MeasureCardItem> getMeasurementList() {
       for(int i=0; i<cardItems.size(); i++)
       {
           System.out.println(cardItems.get(i).getEt().getText().toString());
           cardItems.get(i).setLength(cardItems.get(i).getEt().getText().toString());
       }

       return cardItems;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public EditText length;
        public ImageView iv;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.type);
            length = itemView.findViewById(R.id.inches);
            iv = itemView.findViewById(R.id.mVector);
            int color = length.getCurrentTextColor();
            length.setEnabled(editable);
            length.setTextColor(color);
        }

        public void bind(MeasureCardItem cardItem,int position) {
            textView.setText(cardItem.getTitle());
            Uri uri = cardItem.getImageUri();
            if(uri==null)
            {
                iv.setImageResource(cardItem.getImageResId());
            }
            else
            {
                Glide.with(context.getApplicationContext())
                        .load(uri)
                        .into(iv);
            }

            if(!cardItem.getLength().equals("0"))
                length.setText(cardItem.getLength());
            else
                length.setText("");
            cardItem.setEt(length);
        }

        public EditText getEditText(){return length;}
    }

    class AddCardViewHolder extends RecyclerView.ViewHolder {

        public ImageView plusIcon;

        public AddCardViewHolder(@NonNull View itemView) {
            super(itemView);
            //plusIcon = itemView.findViewById(R.id.plusIcon);
        }
    }

    private class DisplayCardViewHolder extends RecyclerView.ViewHolder {
        TextView bmt, len;
        ImageView imv;
        public DisplayCardViewHolder(View view) {
            super(view);
            bmt = view.findViewById(R.id.bmt);
            len = view.findViewById(R.id.len);
            imv = view.findViewById(R.id.imv);
        }

        public void bind(MeasureCardItem cardItem) {
            bmt.setText(cardItem.getTitle());
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


