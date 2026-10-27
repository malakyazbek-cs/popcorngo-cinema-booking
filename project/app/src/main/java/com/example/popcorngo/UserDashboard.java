package com.example.popcorngo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class UserDashboard extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // --- Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbarUser);
        setSupportActionBar(toolbar);

        username = getIntent().getStringExtra("username");

        if (getSupportActionBar() != null && username != null) {
            getSupportActionBar().setTitle("Welcome " + username);
        }

        // --- Bottom Navigation ---
        BottomNavigationView bottomNav = findViewById(R.id.user_bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_movie) {
                selectedFragment = new UserMovies();
            } else if (id == R.id.nav_snack) {
                selectedFragment = new UserSnacks();
            } else if (id == R.id.nav_mytickets) {
                selectedFragment = new UserTickets();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.user_fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_movie);
        }
    }

    // --- Toolbar menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            Intent intent = new Intent(UserDashboard.this, ProfileActivity.class);
            intent.putExtra("userId", getUserId());
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.menu_logout) {
            Intent intent = new Intent(UserDashboard.this, Loginpage.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getUserId() {
        return getIntent().getIntExtra("userId", -1);
    }

}
