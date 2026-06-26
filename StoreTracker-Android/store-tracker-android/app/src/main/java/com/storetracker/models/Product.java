package com.storetracker.models;

import java.util.UUID;

/**
 * Represents a single product in the inventory.
 * Instances of this class are serialized to JSON via Gson and saved
 * to internal storage in products.json.
 */
public class Product {

    private String id;           // Unique identifier (UUID)
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imagePath;    // Absolute path to image in app's internal files dir

    // No-arg constructor required by Gson for deserialization
    public Product() {}

    public Product(String name, String description, double price, int stock, String imagePath) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    /** Returns true when stock is low (below 5 units) — used for red-flag UI. */
    public boolean isLowStock() {
        return stock < 5;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ") x" + stock;
    }
}
