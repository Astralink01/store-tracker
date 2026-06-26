package com.storetracker.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a completed transaction.
 * Serialized to JSON via Gson and appended to orders.json in internal storage.
 */
public class Order {

    private String id;
    private long timestamp;          // Unix time in milliseconds
    private List<OrderItem> items;
    private double totalCost;
    private double amountTendered;   // Cash given by the buyer
    private double change;           // Change returned to the buyer

    // No-arg constructor required by Gson
    public Order() {}

    public Order(List<OrderItem> items, double totalCost, double amountTendered) {
        this.id              = UUID.randomUUID().toString();
        this.timestamp       = System.currentTimeMillis();
        this.items           = items;
        this.totalCost       = totalCost;
        this.amountTendered  = amountTendered;
        this.change          = amountTendered - totalCost;
    }

    /** Returns a human-readable date/time string for display in order history. */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getAmountTendered() { return amountTendered; }
    public void setAmountTendered(double amountTendered) { this.amountTendered = amountTendered; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }
}
