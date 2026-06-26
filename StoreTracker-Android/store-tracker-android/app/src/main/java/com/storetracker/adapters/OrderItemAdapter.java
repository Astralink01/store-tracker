package com.storetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.storetracker.R;
import com.storetracker.models.OrderItem;

import java.util.List;

/**
 * RecyclerView adapter used inside the Order Detail dialog
 * to list individual line items of a past order.
 */
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderItem> items;

    public OrderItemAdapter(Context context, List<OrderItem> items) {
        this.context = context;
        this.items   = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OrderItem item = items.get(position);
        h.tvName.setText(item.getProductName());
        h.tvQty.setText("x" + item.getQuantity());
        h.tvSubtotal.setText(String.format("$%.2f", item.getSubtotal()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvSubtotal;

        ViewHolder(@NonNull View item) {
            super(item);
            tvName     = item.findViewById(R.id.tvOrderItemName);
            tvQty      = item.findViewById(R.id.tvOrderItemQty);
            tvSubtotal = item.findViewById(R.id.tvOrderItemSubtotal);
        }
    }
}
