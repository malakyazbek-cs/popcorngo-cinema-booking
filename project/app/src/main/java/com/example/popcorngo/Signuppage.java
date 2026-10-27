package com.example.popcorngo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class Signuppage extends AppCompatActivity {

    private EditText etUser, etPass, etConfirm;
    private Button signupbtn, loginbtn;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuppage);

        db = new DatabaseHelper(this);

        etUser = findViewById(R.id.newEmail);
        etPass = findViewById(R.id.newPassword);
        etConfirm = findViewById(R.id.confirmPasswordInput);
        signupbtn = findViewById(R.id.signbtn);
        signupbtn.setOnClickListener(v -> doRegister());

        loginbtn = findViewById(R.id.loginbtn);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signuppage.this, Loginpage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void doRegister() {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.isEmailExists(u)) {
            Toast.makeText(this, "Email already exists. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (p.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!p.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = db.registerUser(u, p);
        if (ok) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("Account created. Remember your credentials.")
                    .setPositiveButton("OK", (d, which) -> {
                        Intent intent = new Intent(Signuppage.this, Loginpage.class);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        } else {
            Toast.makeText(this, "Error fetching data from server.", Toast.LENGTH_SHORT).show();
        }

    }
}