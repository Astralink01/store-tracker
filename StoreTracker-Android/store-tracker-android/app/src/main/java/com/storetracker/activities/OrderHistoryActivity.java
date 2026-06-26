package com.storetracker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.storetracker.R;
import com.storetracker.adapters.OrderHistoryAdapter;
import com.storetracker.adapters.OrderItemAdapter;
import com.storetracker.models.Order;
import com.storetracker.utils.StorageManager;

import java.util.Collections;
import java.util.List;

/**
 * Displays all past transactions loaded from orders.json.
 * Orders are shown newest-first. Tapping any order opens a detail dialog
 * listing every item, the total, payment, and change.
 */
public class OrderHistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderClickListener {

    private RecyclerView    rvOrders;
    private TextView        tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrders = findViewById(R.id.rvOrderHistory);
        tvEmpty  = findViewById(R.id.tvOrderHistoryEmpty);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        loadOrders();
    }

    private void loadOrders() {
        // Deserialize orders from orders.json via Gson inside StorageManager
        List<Order> orders = StorageManager.getInstance(this).loadOrders();

        if (orders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        // Show newest orders at the top
        Collections.reverse(orders);

        rvOrders.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvOrders.setAdapter(new OrderHistoryAdapter(this, orders, this));
    }

    @Override
    public void onOrderClick(Order order) {
        // Inflate a simple dialog showing the order's line items
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_detail, null);

        RecyclerView rvItems = dialogView.findViewById(R.id.rvDialogOrderItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(new OrderItemAdapter(this, order.getItems()));

        TextView tvDate    = dialogView.findViewById(R.id.tvDialogOrderDate);
        TextView tvTotal   = dialogView.findViewById(R.id.tvDialogOrderTotal);
        TextView tvPaid    = dialogView.findViewById(R.id.tvDialogOrderPaid);
        TextView tvChange  = dialogView.findViewById(R.id.tvDialogOrderChange);

        tvDate.setText(order.getFormattedDate());
        tvTotal.setText(String.format("Total:    $%.2f", order.getTotalCost()));
        tvPaid.setText(String.format("Paid:     $%.2f", order.getAmountTendered()));
        tvChange.setText(String.format("Change:   $%.2f", order.getChange()));

        new AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
}
