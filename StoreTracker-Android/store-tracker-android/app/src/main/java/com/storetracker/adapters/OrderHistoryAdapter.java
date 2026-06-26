package com.storetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.storetracker.R;
import com.storetracker.models.Order;

import java.util.List;

/**
 * RecyclerView adapter for the Order History screen.
 * Displays each past order: date, total, item count, and a tap to expand details.
 */
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public OrderHistoryAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context  = context;
        this.orders   = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orders.get(position);

        h.tvDate.setText(order.getFormattedDate());
        h.tvTotal.setText(String.format("Total: $%.2f", order.getTotalCost()));
        h.tvItemCount.setText(order.getItems().size() + " item(s)");

        h.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTotal, tvItemCount;

        ViewHolder(@NonNull View item) {
            super(item);
            tvDate      = item.findViewById(R.id.tvOrderDate);
            tvTotal     = item.findViewById(R.id.tvOrderTotal);
            tvItemCount = item.findViewById(R.id.tvOrderItemCount);
        }
    }
}
