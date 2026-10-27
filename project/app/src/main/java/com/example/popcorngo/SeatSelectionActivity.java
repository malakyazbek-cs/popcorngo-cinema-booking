package com.example.popcorngo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SeatSelectionActivity extends AppCompatActivity {

    private GridLayout gridSeats;
    private Button btnConfirmSeats;
    private DatabaseHelper dbHelper;
    private int showtimeId;
    private List<ToggleButton> seatButtons = new ArrayList<>();
    private List<String[]> seatData = new ArrayList<>();
    private int userId;
    private double ticketPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        gridSeats = findViewById(R.id.gridSeats);
        btnConfirmSeats = findViewById(R.id.btnConfirmSeats);
        dbHelper = new DatabaseHelper(this);

        showtimeId = getIntent().getIntExtra("SHOWTIME_ID", -1);
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (showtimeId == -1 || userId == -1) {
            Toast.makeText(this, "Invalid showtime or user.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketPrice = dbHelper.getTicketPriceForShowtime(showtimeId);
        if (ticketPrice <= 0) {
            Toast.makeText(this, "Could not load ticket price.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAvailableSeats(showtimeId);

        btnConfirmSeats.setOnClickListener(v -> {
            ArrayList<String> selectedSeatNumbers = new ArrayList<>();
            for (int i = 0; i < seatButtons.size(); i++) {
                if (seatButtons.get(i).isChecked()) {
                    selectedSeatNumbers.add(seatData.get(i)[1]);
                }
            }

            if (selectedSeatNumbers.isEmpty()) {
                Toast.makeText(this, "Please select at least one seat.", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalPrice = selectedSeatNumbers.size() * ticketPrice;

            long bookingId = dbHelper.insertBooking(userId, showtimeId, totalPrice,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()), "Pending");

            if (bookingId > 0) {
                boolean allSeatsBooked = true;
                for (String seatNumber : selectedSeatNumbers) {
                    long seatInsertResult = dbHelper.insertBookingSeat((int) bookingId, showtimeId, seatNumber);
                    if (seatInsertResult <= 0) {
                        Log.e("SeatSelectionActivity", "Failed to book seat: " + seatNumber);
                        allSeatsBooked = false;
                    }
                }

                if (allSeatsBooked) {
                    Toast.makeText(this, "Booking successful! You can also buy some snacks or pay for your booking.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SeatSelectionActivity.this, SnackSelectionActivity.class);
                    intent.putExtra("BOOKING_ID", (int) bookingId);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Booking failed for some seats. Please try again.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Failed to create the main booking record.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAvailableSeats(int showtimeId) {
        try {
            seatData = dbHelper.getAvailableSeatsForShowtime(showtimeId);
            gridSeats.removeAllViews();
            seatButtons.clear();

            for (String[] seat : seatData) {
                ToggleButton seatBtn = new ToggleButton(this);
                seatBtn.setTextOff(seat[1]);
                seatBtn.setTextOn(seat[1]);
                seatBtn.setText(seat[1]);
                seatBtn.setTag(seat[0]);
                seatBtn.setLayoutParams(new GridLayout.LayoutParams());
                gridSeats.addView(seatBtn);
                seatButtons.add(seatBtn);
            }
        } catch (Exception e) {
            Log.e("SeatSelectionActivity", "Error loading seats: " + e.getMessage());
            Toast.makeText(this, "Error loading seats. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
