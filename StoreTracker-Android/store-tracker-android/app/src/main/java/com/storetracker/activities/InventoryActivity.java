package com.storetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.storetracker.R;
import com.storetracker.adapters.ProductAdapter;
import com.storetracker.models.Product;
import com.storetracker.utils.StorageManager;

import java.io.File;
import java.util.List;

/**
 * Displays the full product inventory list.
 * Each row supports quick stock adjustment (+/−) and bulk-edit via EditText.
 * The FAB opens AddEditProductActivity for adding new products.
 */
public class InventoryActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> products;
    private StorageManager storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        storage = StorageManager.getInstance(this);

        recyclerView = findViewById(R.id.rvInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAdd = findViewById(R.id.fabAddProduct);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data each time we return to this screen
        // so changes from AddEditProductActivity are reflected
        products = storage.loadProducts();
        adapter  = new ProductAdapter(this, products, this);
        recyclerView.setAdapter(adapter);
    }

    // ── ProductAdapter.OnProductActionListener ──────────────────────────────

    @Override
    public void onEdit(Product product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Delete \"" + product.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Also delete the associated image file from internal storage
                    if (product.getImagePath() != null) {
                        File imgFile = new File(product.getImagePath());
                        if (imgFile.exists()) imgFile.delete();
                    }
                    storage.deleteProduct(product.getId());
                    products.remove(product);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onStockChanged(Product product) {
        // Stock was already persisted inside the adapter — nothing extra needed here
        Toast.makeText(this, "Stock updated: " + product.getName(), Toast.LENGTH_SHORT).show();
    }
}
