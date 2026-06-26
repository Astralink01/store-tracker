package com.storetracker.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.storetracker.R;
import com.storetracker.adapters.OrderItemAdapter;
import com.storetracker.models.Order;
import com.storetracker.models.OrderItem;
import com.storetracker.utils.StorageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Checkout screen — final step of the POS flow.
 *
 * Receives the cart (List<OrderItem>) from POSActivity.
 * Displays order summary, total cost, and a payment input.
 * Automatically computes change as the cashier types.
 * "Done Order" button:
 *   1. Creates an Order object and saves it to orders.json.
 *   2. StorageManager.saveOrder() deducts stock from products.json automatically.
 *   3. Returns to POSActivity with the cart cleared.
 */
public class CheckoutActivity extends AppCompatActivity {

    private List<OrderItem> cartItems;
    private double totalCost = 0.0;

    private TextView tvOrderTotal, tvChange;
    private EditText etPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Receive cart from POSActivity
        cartItems = (List<OrderItem>) getIntent().getSerializableExtra("cart_items");
        if (cartItems == null) cartItems = new ArrayList<>();

        tvOrderTotal = findViewById(R.id.tvCheckoutTotal);
        tvChange     = findViewById(R.id.tvCheckoutChange);
        etPayment    = findViewById(R.id.etPaymentAmount);

        // Calculate order total
        for (OrderItem item : cartItems) totalCost += item.getSubtotal();
        tvOrderTotal.setText(String.format("Order Total:  $%.2f", totalCost));

        // Populate order summary list
        RecyclerView rvSummary = findViewById(R.id.rvCheckoutSummary);
        rvSummary.setLayoutManager(new LinearLayoutManager(this));
        rvSummary.setAdapter(new OrderItemAdapter(this, cartItems));

        // Live change calculation as cashier types payment amount
        etPayment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    tvChange.setText("Change:  —");
                    return;
                }
                try {
                    double payment = Double.parseDouble(text);
                    double change  = payment - totalCost;
                    if (change < 0) {
                        tvChange.setText(String.format("Change:  -$%.2f  (insufficient)", Math.abs(change)));
                        tvChange.setTextColor(getColor(android.R.color.holo_red_dark));
                    } else {
                        tvChange.setText(String.format("Change:  $%.2f", change));
                        tvChange.setTextColor(getColor(android.R.color.holo_green_dark));
                    }
                } catch (NumberFormatException e) {
                    tvChange.setText("Change:  —");
                }
            }
        });

        Button btnDoneOrder = findViewById(R.id.btnDoneOrder);
        btnDoneOrder.setOnClickListener(v -> finalizeOrder());
    }

    /**
     * Finalizes the transaction:
     *  1. Validates payment amount ≥ total.
     *  2. Creates an Order and passes it to StorageManager.saveOrder().
     *  3. saveOrder() writes to orders.json AND deducts stock from products.json.
     *  4. Clears the cart and navigates the user back to POSActivity.
     */
    private void finalizeOrder() {
        String paymentStr = etPayment.getText().toString().trim();
        if (paymentStr.isEmpty()) {
            etPayment.setError("Enter payment amount");
            return;
        }

        double payment;
        try {
            payment = Double.parseDouble(paymentStr);
        } catch (NumberFormatException e) {
            etPayment.setError("Invalid amount");
            return;
        }

        if (payment < totalCost) {
            Toast.makeText(this, "Payment is less than the total!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and persist the order
        Order completedOrder = new Order(cartItems, totalCost, payment);
        StorageManager.getInstance(this).saveOrder(completedOrder);
        // ↑ This single call:
        //   • Appends the order to orders.json
        //   • Subtracts each sold item's quantity from the matching product in products.json

        Toast.makeText(this,
                String.format("Order saved! Change: $%.2f", completedOrder.getChange()),
                Toast.LENGTH_LONG).show();

        // Return all the way back to POSActivity and clear the back stack so
        // the user doesn't accidentally re-submit the same order
        finishAffinity();
        startActivity(new android.content.Intent(this, POSActivity.class));
    }
}
