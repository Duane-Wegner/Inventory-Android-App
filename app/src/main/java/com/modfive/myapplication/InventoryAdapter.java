package com.modfive.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ItemViewHolder> {

    private List<InventoryItem> itemList;
    private OnItemChangeListener listener;

    public interface OnItemChangeListener {
        void onItemDeleted(int position);
        void onQuantityZero(int position);
        void updateQuantity(int position, int newQuantity);
    }

    public InventoryAdapter(List<InventoryItem> itemList, OnItemChangeListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        holder.nameText.setText(item.getName());
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        holder.plusBtn.setOnClickListener(v -> {
            item.increaseQuantity();
            notifyItemChanged(position);
            listener.updateQuantity(position, item.getQuantity()); // New method to update DB
        });

        holder.minusBtn.setOnClickListener(v -> {
            item.decreaseQuantity();
            notifyItemChanged(position);
            listener.updateQuantity(position, item.getQuantity());
            if (item.getQuantity() == 0) {
                listener.onQuantityZero(position);
            }
        });

        holder.deleteBtn.setOnClickListener(v -> {
            listener.onItemDeleted(position);
        });
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText;
        Button plusBtn, minusBtn, deleteBtn;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.itemName);
            quantityText = itemView.findViewById(R.id.quantityText);
            plusBtn = itemView.findViewById(R.id.plusBtn);
            minusBtn = itemView.findViewById(R.id.minusBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
