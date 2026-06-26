package com.storetracker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.storetracker.R;
import com.storetracker.models.Product;
import com.storetracker.utils.StorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Handles both Add and Edit product flows.
 * When called with a "product_id" extra, it pre-fills fields for editing.
 * Image capture is done via Camera intent or Gallery picker;
 * the resulting image is copied into the app's internal files/product_images directory.
 */
public class AddEditProductActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private ImageView ivProductPhoto;
    private EditText  etName, etDescription, etPrice, etStock;
    private Button    btnSave, btnChoosePhoto;

    private StorageManager storage;
    private Product existingProduct; // null when adding new
    private String  currentImagePath; // path to the chosen/captured image
    private Uri     cameraImageUri;   // URI for the camera capture temp file

    // ── ActivityResult launchers ─────────────────────────────────────────────

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Camera wrote to cameraImageUri; copy to internal storage
                    copyUriToInternal(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    copyUriToInternal(selectedUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        storage         = StorageManager.getInstance(this);
        ivProductPhoto  = findViewById(R.id.ivProductPhoto);
        etName          = findViewById(R.id.etProductName);
        etDescription   = findViewById(R.id.etProductDescription);
        etPrice         = findViewById(R.id.etProductPrice);
        etStock         = findViewById(R.id.etProductStock);
        btnSave         = findViewById(R.id.btnSaveProduct);
        btnChoosePhoto  = findViewById(R.id.btnChoosePhoto);

        // Check if we're editing an existing product
        String productId = getIntent().getStringExtra("product_id");
        if (productId != null) {
            setTitle("Edit Product");
            loadExistingProduct(productId);
        } else {
            setTitle("Add Product");
        }

        btnChoosePhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void loadExistingProduct(String productId) {
        List<Product> products = storage.loadProducts();
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                existingProduct  = p;
                currentImagePath = p.getImagePath();
                break;
            }
        }
        if (existingProduct == null) return;

        etName.setText(existingProduct.getName());
        etDescription.setText(existingProduct.getDescription());
        etPrice.setText(String.valueOf(existingProduct.getPrice()));
        etStock.setText(String.valueOf(existingProduct.getStock()));

        if (!TextUtils.isEmpty(currentImagePath)) {
            File imgFile = new File(currentImagePath);
            if (imgFile.exists()) {
                Glide.with(this).load(imgFile).centerCrop().into(ivProductPhoto);
            }
        }
    }

    // ── Image Source ─────────────────────────────────────────────────────────

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Product Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else            openGallery();
                })
                .show();
    }

    private void openCamera() {
        // Check camera permission at runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Create a temp file in the app's internal files dir for the camera output
        File tempFile;
        try {
            tempFile = File.createTempFile("cam_", ".jpg", getFilesDir());
        } catch (IOException e) {
            Toast.makeText(this, "Cannot create temp file", Toast.LENGTH_SHORT).show();
            return;
        }

        // FileProvider converts internal file path to a content:// URI safe for sharing with camera app
        cameraImageUri = FileProvider.getUriForFile(this, "com.storetracker.fileprovider", tempFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        // On Android 13+ use READ_MEDIA_IMAGES; below that use READ_EXTERNAL_STORAGE
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 102);
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    /**
     * Copies the image at sourceUri into the app's private product_images folder.
     * Stores the absolute path in currentImagePath so it can be saved on the Product object.
     */
    private void copyUriToInternal(Uri sourceUri) {
        if (sourceUri == null) return;

        // Generate a unique filename so images don't overwrite each other
        String filename = "product_" + UUID.randomUUID() + ".jpg";
        File destFile   = storage.getProductImageFile(filename);

        try (InputStream in  = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {
            if (in == null) return;
            byte[] buf = new byte[4096];
            int    len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);

            // Update the path variable and refresh the preview image
            currentImagePath = destFile.getAbsolutePath();
            Glide.with(this).load(destFile).centerCrop().into(ivProductPhoto);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Save Logic ────────────────────────────────────────────────────────────

    private void saveProduct() {
        String name  = etName.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            return;
        }

        double price;
        int    stock;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            return;
        }
        try {
            stock = TextUtils.isEmpty(stockStr) ? 0 : Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            etStock.setError("Invalid stock number");
            return;
        }

        if (existingProduct == null) {
            // ── ADD new product ──────────────────────────────────────────────
            Product newProduct = new Product(name, desc, price, stock, currentImagePath);
            storage.addProduct(newProduct);
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
        } else {
            // ── EDIT existing product ────────────────────────────────────────
            existingProduct.setName(name);
            existingProduct.setDescription(desc);
            existingProduct.setPrice(price);
            existingProduct.setStock(stock);
            existingProduct.setImagePath(currentImagePath);
            storage.updateProduct(existingProduct);
            Toast.makeText(this, "Product updated!", Toast.LENGTH_SHORT).show();
        }

        finish(); // Return to InventoryActivity
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) openCamera();
            else                                          openGallery();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
