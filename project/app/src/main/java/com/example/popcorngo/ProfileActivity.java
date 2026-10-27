package com.example.popcorngo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class ProfileActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etCreditCard, etBalance;
    private TextView tvCurrentBalance;
    private ImageButton btnBackToDashboard;
    private Button btnUpdateEmail, btnUpdatePassword, btnUpdateCard, btnAddBalance;
    private DatabaseHelper dbHelper;
    private int userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCreditCard = findViewById(R.id.etCreditCard);
        etBalance = findViewById(R.id.etBalance);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);

        btnBackToDashboard = findViewById(R.id.btn_back);
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnUpdateCard = findViewById(R.id.btnUpdateCard);
        btnAddBalance = findViewById(R.id.btnAddBalance);

        userId = getIntent().getIntExtra("userId", 0);

        loadUserInfo();

        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UserDashboard.class);
            startActivity(intent);
            finish();
        });

        btnUpdateEmail.setOnClickListener(v -> updateEmail());
        btnUpdatePassword.setOnClickListener(v -> updatePassword());
        btnUpdateCard.setOnClickListener(v -> updateCard());
        btnAddBalance.setOnClickListener(v -> updateBalance());
    }

    private void loadUserInfo() {
        if (userId <= 0) {
            Toast.makeText(this, "User ID not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor c = dbHelper.getUserById(userId);

        if (c.moveToFirst()) {
            int emailIndex = c.getColumnIndexOrThrow("email");
            int creditCardIndex = c.getColumnIndexOrThrow("credit_card");
            int balanceIndex = c.getColumnIndexOrThrow("balance");

            etEmail.setText(c.getString(emailIndex));
            etCreditCard.setText(c.getString(creditCardIndex));

            double balance = c.getDouble(balanceIndex);
            tvCurrentBalance.setText("Current Balance : $" + String.format("%.2f", balance));
        }

        c.close();
    }

    private void updateEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.updateEmail(userId, email)) {
            Toast.makeText(this, "Email updated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String pass = etPassword.getText().toString().trim();

        if (pass.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.updatePassword(userId, pass)) {
            Toast.makeText(this, "Password updated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCard() {
        String card = etCreditCard.getText().toString().trim();

        if (card.isEmpty()) {
            Toast.makeText(this, "Enter card number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.updateCreditCard(userId, card)) {
            Toast.makeText(this, "Credit card updated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBalance() {
        String input = etBalance.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Enter an amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(input);

            if (amount <= 0) {
                Toast.makeText(this, "Amount must be positive.", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.updateBalance(userId, amount);
            Toast.makeText(this, "Balance added!", Toast.LENGTH_SHORT).show();
            etBalance.setText("");
            loadUserInfo();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount entered.", Toast.LENGTH_SHORT).show();
        }
    }
}