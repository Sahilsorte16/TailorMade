package com.example.tailmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<OrderItem> list;
    Context context;
    Activity activity;
    Boolean b;
    public ItemListAdaptor(Context context, Activity activity, boolean b)
    {
        list = new ArrayList<>();
        this.context  = context;
        this.activity = activity;
        this.b = b;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if(activity instanceof OrderDetails)
            view = inflater.inflate(R.layout.order_item_list_view_display, parent, false);
        else
            view = inflater.inflate(R.layout.order_item_list, parent, false);
        return new ItemListAdaptor.CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OrderItem orderItem = list.get(position);
        CardViewHolder cardViewHolder = (CardViewHolder)holder;
        cardViewHolder.bind(orderItem);

        cardViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity instanceof AddOrder)
                {
                    Intent intent = new Intent(context, Add_Item.class);
                    intent.putExtra("Cid", ((AddOrder) activity).Cid);
                    intent.putExtra("activity","Edit Item");
                    intent.putExtra("LayoutPosition", holder.getLayoutPosition());
                    intent.putExtra("Item Name", orderItem.getName());
                    intent.putExtra("Item type", orderItem.getType());
                    intent.putExtra("Body Measurements", (Serializable) orderItem.getBodyMs());
                    intent.putExtra("Charges", orderItem.getCharges());
                    intent.putStringArrayListExtra("Instructions", (ArrayList<String>) orderItem.getInstructions());
                    intent.putExtra("Cloth Images", orderItem.getClothImages());
                    intent.putExtra("Pattern Images", orderItem.getPatternImages());
                    activity.startActivityForResult(intent, 123);
                }
                else if(activity instanceof OrderDetails)
                {
                    if(b)
                    {
                        Intent intent = new Intent(context, OrderItemDetails.class);
                        intent.putExtra("Item Name", orderItem.getName());
                        intent.putExtra("Item Type", orderItem.getType());
                        intent.putExtra("Body Measurements", (Serializable) orderItem.getBodyMs());
                        intent.putExtra("Charges", orderItem.getCharges());
                        intent.putExtra("Total amount", orderItem.getTotalItemCharges());
                        intent.putStringArrayListExtra("Instructions", (ArrayList<String>) orderItem.getInstructions());
                        Gson gson = new Gson();
                        String json = gson.toJson(orderItem.getExpenses());
                        intent.putExtra("Expenses",  json);
                        intent.putExtra("Dress Images", orderItem.getDressImages());
                        intent.putExtra("Cloth Images", orderItem.getClothImages());
                        intent.putExtra("Pattern Images", orderItem.getPatternImages());
                        activity.startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(context, PrepareOrder.class);
                        intent.putExtra("Item Name", orderItem.getName());
                        intent.putExtra("Item Type", orderItem.getType());
                        intent.putExtra("Charges", orderItem.getCharges());
                        intent.putExtra("Complete", orderItem.isComplete());
                        intent.putExtra("LayoutPosition", holder.getLayoutPosition());
                        intent.putExtra("Total amount", orderItem.getTotalItemCharges());
                        intent.putExtra("Id", orderItem.getId());
                        Gson gson = new Gson();
                        String json = gson.toJson(orderItem.getExpenses());
                        intent.putExtra("Expenses",  json);
                        intent.putExtra("Dress Images", orderItem.getDressImages());
                        activity.startActivityForResult(intent, 456);
                    }
                }
            }
        });

        cardViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteCardDialog(orderItem, cardViewHolder.getLayoutPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItem(OrderItem orderItem)
    {
        list.add(orderItem);
        notifyDataSetChanged();
    }

    public void updateItem(OrderItem orderItem, int position)
    {
        list.set(position, orderItem);
        notifyDataSetChanged();
    }

    public List<OrderItem> getOrderItems()
    {
        return list;
    }
    public OrderItem getItem(int position)
    {
        return list.get(position);
    }

    private void showDeleteCardDialog(OrderItem item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Delete Measurement")
                .setIcon(R.drawable.baseline_delete_24)
                .setMessage("Do you want to delete '" + item.getName() + "' ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        ((AddOrder)activity).totalAmt.setText("₹ " + makeAmount());
                    }
                })
                .setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public int makeAmount() {
        int ttl=0;
        for(OrderItem o: list)
        {
            ttl += Integer.parseInt(o.getTotalItemCharges());
        }
        return ttl;
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, cost;
        ImageView img;
        public CardViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            type = view.findViewById(R.id.item_type);
            cost = view.findViewById(R.id.item_cost);
            img = view.findViewById(R.id.itemImage);
        }

        public void bind(OrderItem orderItem) {
            name.setText(orderItem.getName());
            type.setText(orderItem.getType());
            if(b)
                cost.setText("₹ " + orderItem.getTotalItemCharges());
            else
            {
                if(orderItem.isComplete())
                {
                    cost.setText("COMPLETED");
                    cost.setTextColor(activity.getColor(R.color.green));
                }
                else
                {
                    cost.setText("INCOMPLETE");
                    cost.setTextColor(activity.getColor(R.color.Delete_red));
                }
            }


            byte[] imageBytes = null;
            if(!orderItem.getDressImages().isEmpty())
                imageBytes = orderItem.getDressImages().get(0);
            else if(!orderItem.getClothImages().isEmpty())
                imageBytes = orderItem.getClothImages().get(0);
            else if(!orderItem.getPatternImages().isEmpty())
                imageBytes = orderItem.getPatternImages().get(0);

            if(imageBytes!=null)
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Glide.with(context)
                        .load(bitmap)
                        .transform(new CenterCrop(), new RoundedCorners(20))
                        .into(img);
            }

        }
    }
}
