package com.storetracker.models;

/**
 * Represents a single line item in a cart or completed order.
 * Stores a snapshot of the product at the time of sale so that
 * order history remains accurate even if the product is later edited.
 */
public class OrderItem {

    private String productId;
    private String productName;
    private double unitPrice;
    private int quantity;

    // No-arg constructor required by Gson
    public OrderItem() {}

    public OrderItem(Product product, int quantity) {
        this.productId   = product.getId();
        this.productName = product.getName();
        this.unitPrice   = product.getPrice();
        this.quantity    = quantity;
    }

    /** Calculates the subtotal for this line item. */
    public double getSubtotal() {
        return unitPrice * quantity;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
