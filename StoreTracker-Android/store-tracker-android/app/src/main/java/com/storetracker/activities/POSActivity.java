package com.storetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.storetracker.R;
import com.storetracker.adapters.CartAdapter;
import com.storetracker.adapters.ProductAdapter;
import com.storetracker.models.OrderItem;
import com.storetracker.models.Product;
import com.storetracker.utils.StorageManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Point-of-Sale screen.
 *
 * Left panel  — scrollable product list (tap to add to cart)
 * Right panel — live cart with quantity adjust and total running total
 *
 * On phones, these are stacked vertically; ConstraintLayout handles responsiveness.
 *
 * When the user taps "Checkout", the cart is passed to CheckoutActivity as a Serializable list.
 */
public class POSActivity extends AppCompatActivity {

    private List<OrderItem> cartItems   = new ArrayList<>();
    private List<Product>   products    = new ArrayList<>();
    private CartAdapter     cartAdapter;
    private TextView        tvCartTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        tvCartTotal = findViewById(R.id.tvCartTotal);

        setupProductList();
        setupCartList();

        Button btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // Pass cart to CheckoutActivity for payment and finalization
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("cart_items", (Serializable) cartItems);
            startActivity(intent);
        });

        Button btnClearCart = findViewById(R.id.btnClearCart);
        btnClearCart.setOnClickListener(v -> {
            cartItems.clear();
            cartAdapter.notifyDataSetChanged();
            updateTotal();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the product list so freshly-added inventory appears immediately
        products = StorageManager.getInstance(this).loadProducts();
        setupProductList();
    }

    private void setupProductList() {
        RecyclerView rvProducts = findViewById(R.id.rvPOSProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // When user taps a product, add it to the cart (or increment quantity if already there)
        ProductAdapter productAdapter = new ProductAdapter(this, products, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                // Not used in POS mode
            }

            @Override
            public void onDelete(Product product) {
                // Not used in POS mode
            }

            @Override
            public void onStockChanged(Product product) {
                // Not used in POS mode
            }
        });

        rvProducts.setAdapter(productAdapter);

        // Use item click via touch listener on RecyclerView to add to cart
        rvProducts.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {});

        // Override: tap anywhere on the product card → add to cart
        // Handled in a custom OnClickListener set on each ViewHolder in POSProductAdapter
        setupPOSProductTap(rvProducts);
    }

    /** Re-attaches a simple tap handler so tapping a product row adds it to the cart. */
    private void setupPOSProductTap(RecyclerView rv) {
        rv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(android.view.View view) {
                view.setOnClickListener(v -> {
                    int pos = rv.getChildAdapterPosition(view);
                    if (pos == RecyclerView.NO_ID) return;
                    Product tapped = products.get(pos);
                    if (tapped.getStock() <= 0) {
                        Toast.makeText(POSActivity.this,
                                tapped.getName() + " is out of stock", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addToCart(tapped);
                });
            }

            @Override
            public void onChildViewDetachedFromWindow(android.view.View view) {}
        });
    }

    private void setupCartList() {
        RecyclerView rvCart = findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(this, cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged() { updateTotal(); }

            @Override
            public void onItemRemoved(int position) { updateTotal(); }
        });
        rvCart.setAdapter(cartAdapter);
    }

    /**
     * Adds one unit of the given product to the cart.
     * If the product is already in the cart, increments its quantity.
     */
    private void addToCart(Product product) {
        for (OrderItem item : cartItems) {
            if (item.getProductId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                cartAdapter.notifyDataSetChanged();
                updateTotal();
                return;
            }
        }
        // Product not yet in cart — add a new OrderItem with quantity 1
        cartItems.add(new OrderItem(product, 1));
        cartAdapter.notifyItemInserted(cartItems.size() - 1);
        updateTotal();
    }

    /** Recalculates and displays the running cart total. */
    private void updateTotal() {
        double total = 0;
        for (OrderItem item : cartItems) total += item.getSubtotal();
        tvCartTotal.setText(String.format("Total: $%.2f", total));
    }
}
