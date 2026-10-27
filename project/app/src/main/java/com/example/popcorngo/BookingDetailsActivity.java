package com.example.popcorngo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class BookingDetailsActivity extends AppCompatActivity {

    private static final String TAG = "BookingDetailsActivity";
    private DatabaseHelper dbHelper;
    private int bookingId;
    private int currentUserId;
    private double bookingPrice = 0.0;
    private String bookingStatus = "";

    private TextView tvBookingId, tvMovieTitle, tvShowtime, tvBookingStatus, tvSeatsList, tvTotalPrice;
    private LinearLayout llSnacksContainer, paymentActionsContainer;
    private Button btnCancelBooking, btnPayBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        bookingId = intent.getIntExtra("BOOKING_ID", -1);

        currentUserId = getIntent().getIntExtra("userId", -1);

        if (bookingId == -1) {
            Toast.makeText(this, "Invalid booking ID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        loadBookingDetails(bookingId);
    }

    private void initViews() {
        tvBookingId = findViewById(R.id.tv_booking_id);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvShowtime = findViewById(R.id.tv_showtime);
        tvBookingStatus = findViewById(R.id.tv_booking_status);
        tvSeatsList = findViewById(R.id.tv_seats_list);
        llSnacksContainer = findViewById(R.id.ll_snacks_container);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        paymentActionsContainer = findViewById(R.id.payment_actions_container);
        btnCancelBooking = findViewById(R.id.btn_cancel_booking);
        btnPayBooking = findViewById(R.id.btn_pay_booking);

        btnPayBooking.setOnClickListener(v -> handlePayment());
        btnCancelBooking.setOnClickListener(v -> handleCancellation());
    }

    private void loadBookingDetails(int id) {
        Cursor cursor = dbHelper.getBookingDetails(id);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                String movieTitle = cursor.getString(cursor.getColumnIndexOrThrow("movie_title"));
                String showtimeDate = cursor.getString(cursor.getColumnIndexOrThrow("showtime_date"));
                String showtimeTime = cursor.getString(cursor.getColumnIndexOrThrow("showtime_time"));
                int screenNumber = cursor.getInt(cursor.getColumnIndexOrThrow("screen_number"));
                bookingPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));
                bookingStatus = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                tvBookingId.setText(String.format(Locale.getDefault(), "ID : #%d", id));
                tvMovieTitle.setText(movieTitle);
                tvShowtime.setText(String.format(Locale.getDefault(), "%s @ %s (Screen %d)", showtimeDate, showtimeTime, screenNumber));
                tvBookingStatus.setText(bookingStatus);
                tvTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", bookingPrice));

                updateStatusUI(bookingStatus);

                loadSeatsAndSnacks(id);

            } catch (Exception e) {
                Log.e(TAG, "Error processing booking details cursor: " + e.getMessage());
                Toast.makeText(this, "Error loading details.", Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "Booking not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateStatusUI(String status) {
        if (status.equals("Pending")) {
            paymentActionsContainer.setVisibility(View.VISIBLE);
            tvBookingStatus.setTextColor(Color.parseColor("#FF9800"));
        } else {
            paymentActionsContainer.setVisibility(View.GONE);
            if (status.equals("Paid")) {
                tvBookingStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else if (status.equals("Cancelled")) {
                tvBookingStatus.setTextColor(Color.parseColor("#F44336"));
            }
        }
    }

    private void loadSeatsAndSnacks(int id) {
        List<String> seats = dbHelper.getSeatsForBooking(id);
        StringBuilder seatsText = new StringBuilder();

        if (seats != null && !seats.isEmpty()) {
            for (int i = 0; i < seats.size(); i++) {
                seatsText.append(seats.get(i));
                if (i < seats.size() - 1) {
                    seatsText.append(", ");
                }
            }
        } else {
            seatsText.append("No seats booked");
        }

        tvSeatsList.setText(seatsText.toString());

        llSnacksContainer.removeAllViews();
        List<String[]> snacks = dbHelper.getSnacksForBooking(id);

        if (snacks != null && !snacks.isEmpty()) {
            for (String[] snack : snacks) {
                String name = snack[0];
                int quantity = Integer.parseInt(snack[1]);

                Cursor snackCursor = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT price FROM " + DatabaseHelper.T_SNACK + " WHERE name = ?",
                        new String[]{name});

                if (snackCursor != null && snackCursor.moveToFirst()) {
                    double price = snackCursor.getDouble(snackCursor.getColumnIndexOrThrow("price"));
                    addSnackItem(name, quantity, price);
                    snackCursor.close();
                }
            }
        } else {
            TextView noSnacksText = new TextView(this);
            noSnacksText.setText("No snacks ordered");
            llSnacksContainer.addView(noSnacksText);
        }
    }

    private void addSnackItem(String name, int quantity, double price) {
        TextView tv = new TextView(this);
        double totalItemPrice = quantity * price;
        tv.setText(String.format(Locale.getDefault(), "%s x %d - $%.2f", name, quantity, totalItemPrice));
        llSnacksContainer.addView(tv);
    }

    private void handlePayment() {
        double currentBalance = dbHelper.getUserBalance(currentUserId);

        if (currentBalance < bookingPrice) {
            Toast.makeText(this, "Not enough balance to pay for booking.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Insufficient funds for User ID : " + currentUserId + ". Needed: " + bookingPrice + ", Balance: " + currentBalance);
            return;
        }

        boolean success = dbHelper.payForBooking(bookingId, currentUserId, bookingPrice);

        if (success) {
            Toast.makeText(this, "Payment successful! Booking is now Paid.", Toast.LENGTH_LONG).show();
            bookingStatus = "Paid";
            tvBookingStatus.setText(bookingStatus);
            updateStatusUI(bookingStatus);
        } else {
            Toast.makeText(this, "Payment failed due to a database error.", Toast.LENGTH_LONG).show();
        }
    }

    private void handleCancellation() {
        boolean success = dbHelper.cancelBooking(bookingId);

        if (success) {
            Toast.makeText(this, "Booking successfully cancelled.", Toast.LENGTH_LONG).show();
            bookingStatus = "Cancelled";
            tvBookingStatus.setText(bookingStatus);
            updateStatusUI(bookingStatus);
        } else {
            Toast.makeText(this, "Cancellation failed. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}