package com.example.tailmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MeasureCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_ADD_CARD = 1;
    private final Context context;
    private List<MeasureCardItem> cardItems;
    boolean editable;

    public MeasureCardAdapter(List<MeasureCardItem> cardItems, Context context, boolean editable) {
        this.cardItems = cardItems;
        this.context = context;
        this.editable = editable;
    }

    @Override
    public int getItemViewType(int position) {
        // Return the appropriate view type based on the position
        if (position < cardItems.size()) {
            return VIEW_TYPE_CARD;
        } else {
            return VIEW_TYPE_ADD_CARD;
        }
    }

    public void addItem(MeasureCardItem cardItem) {
        cardItems.add(cardItem);
        notifyItemInserted(cardItems.size() - 1);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CARD) {
            View view = inflater.inflate(R.layout.measurement_card, parent, false);
            return new CardViewHolder(view);
        } else if (editable && viewType == VIEW_TYPE_ADD_CARD) {
            View view = inflater.inflate(R.layout.add_card, parent, false);
            return new AddCardViewHolder(view);
        }

        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardViewHolder) {
            MeasureCardItem cardItem = cardItems.get(position);
            ((CardViewHolder) holder).bind(cardItem,position);

            if(editable)
            {
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
            }


        } else if (holder instanceof AddCardViewHolder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddCardDialog();
                }
            });
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
                            MeasureCardItem newCard = new MeasureCardItem(parameter1);
                            newCard.setRemovable(true);
                            addItem(newCard);
                            notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
            if(!editable)
                length.setEnabled(false);
            else
                length.setEnabled(true);

        }

        public void bind(MeasureCardItem cardItem,int position) {
            textView.setText(cardItem.getTitle());
            iv.setImageResource(cardItem.getImageResId());
        }

    }

    class AddCardViewHolder extends RecyclerView.ViewHolder {

        public ImageView plusIcon;

        public AddCardViewHolder(@NonNull View itemView) {
            super(itemView);
            //plusIcon = itemView.findViewById(R.id.plusIcon);
        }
    }
}


