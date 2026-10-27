package com.example.popcorngo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;


public class SnackSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SNACK_ID = "snack_id";
    public static final String EXTRA_BOOKING_ID = "BOOKING_ID";

    private DatabaseHelper dbHelper;
    private int selectedSnackId;
    private int targetBookingId;
    private Snack currentSnack;

    private ImageView snackImage;
    private TextView snackName;
    private TextView snackPrice;
    private TextView snackStock;
    private EditText editTextQuantity;
    private Button buttonBuySnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack_selection);

        snackImage = findViewById(R.id.snack_image);
        snackName = findViewById(R.id.snack_name);
        snackPrice = findViewById(R.id.snack_price);
        snackStock = findViewById(R.id.snack_stock);
        editTextQuantity = findViewById(R.id.edit_text_quantity);
        buttonBuySnack = findViewById(R.id.button_buy_snack);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        selectedSnackId = intent.getIntExtra(EXTRA_SNACK_ID, -1);
        targetBookingId = intent.getIntExtra(EXTRA_BOOKING_ID, -1);

        if (selectedSnackId == -1 || targetBookingId == -1) {
            Toast.makeText(this, "Error : Missing snack or booking information.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadSnackDetails();
    }

    private void loadSnackDetails() {
        currentSnack = dbHelper.getSnackById(selectedSnackId);

        if (currentSnack != null) {
            snackName.setText(currentSnack.name);
            snackPrice.setText(String.format(Locale.getDefault(), "Price : %.2f $", currentSnack.price));
            snackStock.setText(String.format(Locale.getDefault(), "In Stock : %d units", currentSnack.stock));

            int imageResId = getSnackDrawableId(this, currentSnack.imageUrl);
            if (imageResId != 0) {
                snackImage.setImageResource(imageResId);
            } else {
                snackImage.setImageResource(R.drawable.imgplaceholder);
            }

            if (currentSnack.stock <= 0) {
                buttonBuySnack.setEnabled(true);
                buttonBuySnack.setText("Out of Stock");
                buttonBuySnack.setTextColor(getResources().getColor(android.R.color.black));
                buttonBuySnack.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                snackStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                buttonBuySnack.setOnClickListener(v ->
                        Snackbar.make(v, currentSnack.name + " is currently Out of Stock.", Snackbar.LENGTH_LONG).show()
                );
            } else {
                buttonBuySnack.setEnabled(true);
                buttonBuySnack.setText("Add to Booking");
                buttonBuySnack.setOnClickListener(v -> attemptSnackPurchase(v));
            }

        } else {
            Toast.makeText(this, "Snack not found in database.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void attemptSnackPurchase(View v) {
        if (currentSnack == null) return;

        if (currentSnack.stock <= 0) {
            Snackbar.make(v, currentSnack.name + " is currently Out of Stock.", Snackbar.LENGTH_LONG).show();
            return;
        }

        String quantityStr = editTextQuantity.getText().toString();

        if (quantityStr.isEmpty() || quantityStr.equals("0")) {
            editTextQuantity.setError("Enter a valid quantity");
            return;
        }

        int quantity = Integer.parseInt(quantityStr);

        if (quantity > currentSnack.stock) {
            Snackbar.make(v, "Cannot order " + quantity + " units. Only " + currentSnack.stock + " in stock.", Snackbar.LENGTH_LONG).show();
            return;
        }

        boolean success = dbHelper.insertBookingSnack(targetBookingId, selectedSnackId, quantity);

        if (success) {
            Snackbar.make(v, quantity + " x " + currentSnack.name + " added to booking successfully!", Snackbar.LENGTH_LONG).show();
            loadSnackDetails();
            editTextQuantity.setText("1");
        } else {
            Snackbar.make(v, "Failed to add snack. Please try again.", Snackbar.LENGTH_LONG).show();
        }
    }

    private int getSnackDrawableId(Context context, String imageName) {
        String cleanedName = imageName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        return context.getResources().getIdentifier(cleanedName, "drawable", context.getPackageName());
    }
}
