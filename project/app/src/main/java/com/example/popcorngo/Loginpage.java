package com.example.popcorngo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Loginpage extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button loginbtn, signupbtn;
    private DatabaseHelper db;
    private static final String ADMIN_USER = "malak";
    private static final String ADMIN_PASS = "0000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        db = new DatabaseHelper(this);

        db.deleteOldShowtimesAndUpdateStatus();

        etUser = findViewById(R.id.emailInput);
        etPass = findViewById(R.id.passwordInput);
        loginbtn = findViewById(R.id.loginButton);
        signupbtn = findViewById(R.id.signUpButton);

        loginbtn.setOnClickListener(v -> doLogin());

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Loginpage.this, Signuppage.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void doLogin() {
        String u = etUser.getText().toString().trim();
        String p = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Please : Enter username & password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ADMIN_USER.equals(u) && ADMIN_PASS.equals(p)) {
            Intent admin = new Intent(Loginpage.this, AdminDashboard.class);
            admin.putExtra("username", ADMIN_USER);
            startActivity(admin);
            finish();
            return;
        }

        if (db.checkUser(u, p)) {
            int userId = db.getUserId(u);
            Intent user = new Intent(Loginpage.this, UserDashboard.class);
            user.putExtra("userId", userId);
            user.putExtra("username", u);
            startActivity(user);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
        }

    }
}
