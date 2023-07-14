package com.example.tailmate;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<String> instructions;
    Activity activity;
    public InstructionsAdapter(List<String> instructions, Activity activity)
    {
        this.instructions = instructions;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.instruction_recycler_view_item, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String item = instructions.get(position);
        InstructionViewHolder i = (InstructionViewHolder) holder;
        i.bind(item, position);
        i.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos= i.getLayoutPosition();
                instructions.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    private class InstructionViewHolder extends RecyclerView.ViewHolder {

        TextView instr;
        public ImageView del;
        public InstructionViewHolder(View view) {
            super(view);
            instr = view.findViewById(R.id.instruction);
            del = view.findViewById(R.id.del);
            if(activity instanceof OrderItemDetails)
                del.setVisibility(View.GONE);
        }

        public void bind(String item, int position) {
            instr.setText(item);
        }
    }
}
