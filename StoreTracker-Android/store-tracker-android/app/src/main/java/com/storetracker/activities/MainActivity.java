package com.storetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.storetracker.R;

/**
 * Home screen — provides navigation to the four main sections of the app.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnInventory    = findViewById(R.id.btnInventory);
        Button btnPOS          = findViewById(R.id.btnPOS);
        Button btnOrderHistory = findViewById(R.id.btnOrderHistory);
        Button btnCalculator   = findViewById(R.id.btnCalculator);

        btnInventory.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));

        btnPOS.setOnClickListener(v ->
                startActivity(new Intent(this, POSActivity.class)));

        btnOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));

        btnCalculator.setOnClickListener(v ->
                startActivity(new Intent(this, CalculatorActivity.class)));
    }
}
