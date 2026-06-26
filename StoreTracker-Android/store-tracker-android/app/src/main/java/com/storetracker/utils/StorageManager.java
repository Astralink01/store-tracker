package com.storetracker.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.storetracker.models.Order;
import com.storetracker.models.Product;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton utility that handles ALL file-based persistence.
 *
 * Strategy:
 *   - Products  → products.json  (complete list overwritten on every save)
 *   - Orders    → orders.json    (complete list overwritten on every save)
 *
 * Both files live in Context.getFilesDir() — the app's private internal storage.
 * No database is used anywhere in the app.
 *
 * Gson converts Java objects ↔ JSON strings so they can be written to plain text files.
 */
public class StorageManager {

    private static final String TAG             = "StorageManager";
    private static final String PRODUCTS_FILE   = "products.json";
    private static final String ORDERS_FILE     = "orders.json";

    private static StorageManager instance;

    // Gson instance with pretty printing for readable JSON files
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Context context;

    // Private constructor — access only via getInstance()
    private StorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Returns the singleton instance, creating it on first call. */
    public static synchronized StorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new StorageManager(context);
        }
        return instance;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRODUCT OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Loads the product list from products.json.
     * Gson deserializes the JSON array back into a List<Product>.
     * Returns an empty list if the file does not yet exist.
     */
    public List<Product> loadProducts() {
        File file = new File(context.getFilesDir(), PRODUCTS_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            // TypeToken is needed because Gson can't infer generic type at runtime
            Type listType = new TypeToken<List<Product>>() {}.getType();
            List<Product> products = gson.fromJson(reader, listType);
            return products != null ? products : new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "Error reading products.json", e);
            return new ArrayList<>();
        }
    }

    /**
     * Overwrites products.json with the current product list.
     * Gson serializes each Product object to its JSON representation.
     */
    public void saveProducts(List<Product> products) {
        File file = new File(context.getFilesDir(), PRODUCTS_FILE);
        try (FileWriter writer = new FileWriter(file, false)) {
            // gson.toJson() converts List<Product> → JSON array string
            gson.toJson(products, writer);
        } catch (IOException e) {
            Log.e(TAG, "Error writing products.json", e);
        }
    }

    /** Adds a new product and persists the updated list. */
    public void addProduct(Product product) {
        List<Product> products = loadProducts();
        products.add(product);
        saveProducts(products);
    }

    /**
     * Updates an existing product in-place by matching its ID,
     * then persists the updated list.
     */
    public void updateProduct(Product updated) {
        List<Product> products = loadProducts();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(updated.getId())) {
                products.set(i, updated);
                break;
            }
        }
        saveProducts(products);
    }

    /** Removes a product by ID and persists the updated list. */
    public void deleteProduct(String productId) {
        List<Product> products = loadProducts();
        products.removeIf(p -> p.getId().equals(productId));
        saveProducts(products);
    }

    /**
     * Subtracts sold quantities from inventory stock after an order is finalized.
     * This is the stock reflection step that links the POS flow back to inventory.
     *
     * @param order The completed order whose items must reduce stock.
     */
    public void deductStockForOrder(Order order) {
        List<Product> products = loadProducts();

        for (com.storetracker.models.OrderItem item : order.getItems()) {
            for (Product product : products) {
                if (product.getId().equals(item.getProductId())) {
                    // Subtract sold quantity — floor at 0 to avoid negative stock
                    int newStock = Math.max(0, product.getStock() - item.getQuantity());
                    product.setStock(newStock);
                    break;
                }
            }
        }

        // One write covers all deductions — atomic from the file's perspective
        saveProducts(products);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ORDER OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Loads all past orders from orders.json.
     * Returns an empty list if no orders have been saved yet.
     */
    public List<Order> loadOrders() {
        File file = new File(context.getFilesDir(), ORDERS_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Order>>() {}.getType();
            List<Order> orders = gson.fromJson(reader, listType);
            return orders != null ? orders : new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "Error reading orders.json", e);
            return new ArrayList<>();
        }
    }

    /**
     * Saves a completed order:
     *   1. Loads the current order history list.
     *   2. Appends the new order.
     *   3. Overwrites orders.json with the updated list.
     *   4. Deducts sold quantities from inventory stock.
     */
    public void saveOrder(Order order) {
        List<Order> orders = loadOrders();
        orders.add(order);

        File file = new File(context.getFilesDir(), ORDERS_FILE);
        try (FileWriter writer = new FileWriter(file, false)) {
            gson.toJson(orders, writer);
        } catch (IOException e) {
            Log.e(TAG, "Error writing orders.json", e);
        }

        // Reflect sold quantities back into inventory immediately after saving the order
        deductStockForOrder(order);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IMAGE STORAGE HELPER
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns the File object for a new product image in the app's internal storage.
     * The caller is responsible for copying/moving the actual image bytes here.
     *
     * @param filename e.g. "product_uuid.jpg"
     */
    public File getProductImageFile(String filename) {
        File dir = new File(context.getFilesDir(), "product_images");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, filename);
    }
}
