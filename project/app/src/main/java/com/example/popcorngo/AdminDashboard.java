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


public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome Malak!");
        }

        // --- Bottom Navigation ---
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (id == R.id.nav_movies) {
                selectedFragment = new MoviesFragment();
            } else if (id == R.id.nav_ss) {
                selectedFragment = new ShowtimesSeatsFragment();
            } else if (id == R.id.nav_snacks) {
                selectedFragment = new SnacksFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    // --- Toolbar menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_bookings) {
            Intent intent = new Intent(AdminDashboard.this, AdminBookings.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.menu_reports) {
            Intent intent = new Intent(AdminDashboard.this, ReportActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.menu_logout) {
            Intent intent = new Intent(AdminDashboard.this, Loginpage.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
