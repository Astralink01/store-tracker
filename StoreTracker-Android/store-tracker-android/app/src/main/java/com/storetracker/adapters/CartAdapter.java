package com.storetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.storetracker.R;
import com.storetracker.models.OrderItem;

import java.util.List;

/**
 * RecyclerView adapter for the active shopping cart in POSActivity.
 * Each row shows product name, unit price, quantity, subtotal, and
 * buttons to increase / decrease / remove the item.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    private final Context context;
    private final List<OrderItem> cartItems;
    private final OnCartItemChangeListener listener;

    public CartAdapter(Context context, List<OrderItem> cartItems, OnCartItemChangeListener listener) {
        this.context   = context;
        this.cartItems = cartItems;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OrderItem item = cartItems.get(position);

        h.tvName.setText(item.getProductName());
        h.tvUnitPrice.setText(String.format("$%.2f each", item.getUnitPrice()));
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));
        h.tvSubtotal.setText(String.format("$%.2f", item.getSubtotal()));

        h.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onQuantityChanged();
        });

        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                listener.onQuantityChanged();
            } else {
                // Quantity reached 0 — remove the item from the cart
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
                listener.onItemRemoved(position);
            }
        });

        h.btnRemove.setOnClickListener(v -> {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            listener.onItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvName, tvUnitPrice, tvQuantity, tvSubtotal;
        ImageButton btnPlus, btnMinus, btnRemove;

        ViewHolder(@NonNull View item) {
            super(item);
            tvName      = item.findViewById(R.id.tvCartItemName);
            tvUnitPrice = item.findViewById(R.id.tvCartUnitPrice);
            tvQuantity  = item.findViewById(R.id.tvCartQty);
            tvSubtotal  = item.findViewById(R.id.tvCartSubtotal);
            btnPlus     = item.findViewById(R.id.btnCartPlus);
            btnMinus    = item.findViewById(R.id.btnCartMinus);
            btnRemove   = item.findViewById(R.id.btnCartRemove);
        }
    }
}
