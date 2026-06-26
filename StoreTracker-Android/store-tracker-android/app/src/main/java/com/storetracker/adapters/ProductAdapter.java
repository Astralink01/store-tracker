package com.storetracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.storetracker.R;
import com.storetracker.models.Product;
import com.storetracker.utils.StorageManager;

import java.io.File;
import java.util.List;

/**
 * RecyclerView adapter for the inventory product list.
 * Each row shows product name, price, stock, image, and
 * quick-adjust [+] / [−] buttons with an optional bulk-entry EditText.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onStockChanged(Product product);
    }

    private final Context context;
    private final List<Product> products;
    private final OnProductActionListener listener;

    public ProductAdapter(Context context, List<Product> products, OnProductActionListener listener) {
        this.context  = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Product p = products.get(position);

        h.tvName.setText(p.getName());
        h.tvPrice.setText(String.format("$%.2f", p.getPrice()));
        h.tvStock.setText("Stock: " + p.getStock());

        // Low-stock alert: turn stock count red when below 5 units
        if (p.isLowStock()) {
            h.tvStock.setTextColor(Color.parseColor("#D32F2F")); // Material Red 700
            h.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Red 50 tint
        } else {
            h.tvStock.setTextColor(Color.parseColor("#388E3C")); // Material Green 700
            h.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // Load product image from internal storage using Glide
        if (!TextUtils.isEmpty(p.getImagePath())) {
            File imgFile = new File(p.getImagePath());
            if (imgFile.exists()) {
                Glide.with(context).load(imgFile).centerCrop().into(h.ivProduct);
            } else {
                h.ivProduct.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            h.ivProduct.setImageResource(R.drawable.ic_image_placeholder);
        }

        // ── Quick-adjust stock buttons ──────────────────────────────────────

        h.btnIncrement.setOnClickListener(v -> {
            int adj = getAdjustAmount(h.etAdjust);
            p.setStock(p.getStock() + adj);
            StorageManager.getInstance(context).updateProduct(p);
            notifyItemChanged(position);
            listener.onStockChanged(p);
        });

        h.btnDecrement.setOnClickListener(v -> {
            int adj = getAdjustAmount(h.etAdjust);
            p.setStock(Math.max(0, p.getStock() - adj));
            StorageManager.getInstance(context).updateProduct(p);
            notifyItemChanged(position);
            listener.onStockChanged(p);
        });

        h.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    /** Reads the EditText value; defaults to 1 if empty or zero. */
    private int getAdjustAmount(EditText et) {
        String text = et.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return 1;
        try {
            int val = Integer.parseInt(text);
            return val > 0 ? val : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView  cardView;
        ImageView ivProduct;
        TextView  tvName, tvPrice, tvStock;
        ImageButton btnIncrement, btnDecrement, btnEdit, btnDelete;
        EditText  etAdjust;

        ViewHolder(@NonNull View item) {
            super(item);
            cardView     = item.findViewById(R.id.cardProduct);
            ivProduct    = item.findViewById(R.id.ivProduct);
            tvName       = item.findViewById(R.id.tvProductName);
            tvPrice      = item.findViewById(R.id.tvProductPrice);
            tvStock      = item.findViewById(R.id.tvProductStock);
            btnIncrement = item.findViewById(R.id.btnIncrement);
            btnDecrement = item.findViewById(R.id.btnDecrement);
            btnEdit      = item.findViewById(R.id.btnEditProduct);
            btnDelete    = item.findViewById(R.id.btnDeleteProduct);
            etAdjust     = item.findViewById(R.id.etStockAdjust);
        }
    }
}
