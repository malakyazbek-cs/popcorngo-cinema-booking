package com.example.popcorngo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class AddEditSnackActivity extends AppCompatActivity {

    private EditText etName, etPrice, etStock;
    private ImageView ivSnackImage;
    private Button btnCancel, btnSave, btnUploadImage;
    private DatabaseHelper dbHelper;
    private int snackId = -1;
    private String currentImageUrl = "default_snack";
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_snack);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            ivSnackImage.setImageURI(uri);
                            currentImageUrl = uri.toString();
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Toast.makeText(this, "Image selected successfully.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        etName = findViewById(R.id.etSnackName);
        etPrice = findViewById(R.id.etSnackPrice);
        etStock = findViewById(R.id.etSnackStock);
        ivSnackImage = findViewById(R.id.ivSnackImage);
        btnCancel = findViewById(R.id.btnCancelSnack);
        btnSave = findViewById(R.id.btnSaveSnack);
        btnUploadImage = findViewById(R.id.btnUploadSnackImage);

        dbHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("snack_id")) {
            snackId = getIntent().getIntExtra("snack_id", -1);
            loadSnackDetailsForEditing(snackId);
        } else {
            ivSnackImage.setImageResource(R.drawable.imgplaceholder);
        }

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSnack());
        btnUploadImage.setOnClickListener(v -> handleImageUpload());
    }

    private void loadSnackDetailsForEditing(int id) {
        Snack snack = dbHelper.getSnackById(id);
        if (snack != null) {
            etName.setText(snack.name);
            etPrice.setText(String.valueOf(snack.price));
            etStock.setText(String.valueOf(snack.stock));
            currentImageUrl = snack.imageUrl;

            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                if (currentImageUrl.startsWith("content:") || currentImageUrl.startsWith("file:")) {
                    try {
                        Uri imageUri = Uri.parse(currentImageUrl);
                        ivSnackImage.setImageURI(imageUri);
                    } catch (Exception e) {
                        ivSnackImage.setImageResource(R.drawable.imgplaceholder);
                    }
                } else {
                    int resourceId = getResources().getIdentifier(
                            currentImageUrl,
                            "drawable",
                            getPackageName()
                    );

                    if (resourceId != 0) {
                        ivSnackImage.setImageResource(resourceId);
                    } else {
                        ivSnackImage.setImageResource(R.drawable.imgplaceholder);
                    }
                }
            }
        }
    }

    private void handleImageUpload() {
        imagePickerLauncher.launch("image/*");
    }


    private void saveSnack() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String imageUrl = currentImageUrl;

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;

        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or stock", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (snackId == -1) {
            success = dbHelper.addSnack(name, price, stock, imageUrl);
            if (success) Toast.makeText(this, "Snack added successfully", Toast.LENGTH_SHORT).show();
        } else {
            success = dbHelper.updateSnack(snackId, name, price, stock, imageUrl);
            if (success) Toast.makeText(this, "Snack updated successfully", Toast.LENGTH_SHORT).show();
        }

        if (success) finish();
        else Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
    }
}