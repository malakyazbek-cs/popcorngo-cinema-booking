package com.example.popcorngo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class BookingSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String[]> activeBookings;
    private int userId;
    private int snackId;
    private DatabaseHelper dbHelper;

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SNACK_ID = "snack_id";
    private static final String KEY_BOOKING_ID = "BOOKING_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_selection);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        userId = intent.getIntExtra(KEY_USER_ID, -1);
        snackId = intent.getIntExtra(KEY_SNACK_ID, -1);

        if (userId == -1 || snackId == -1) {
            Toast.makeText(this, "Error : Missing User or Snack ID.", Toast.LENGTH_LONG).show();
            Log.e("BookingSelectionActivity", "Missing required intent extras. User ID: " + userId + ", Snack ID: " + snackId);
            finish();
            return;
        }

        Log.d("BookingSelectionActivity", "Loaded User ID: " + userId + ", Snack ID: " + snackId);

        activeBookings = getActiveBookingsForUser(userId);

        recyclerView = findViewById(R.id.recyclerView_active_bookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new BookingAdapter(activeBookings));
    }

    private void handleBookingSelection(String bookingId) {
        Log.d("BookingSelectionActivity", "Booking Selected : ID = " + bookingId + ". Navigating to SnackSelectionActivity.");

        Intent nextIntent = new Intent(BookingSelectionActivity.this, SnackSelectionActivity.class);

        int bookingIntId = Integer.parseInt(bookingId);

        nextIntent.putExtra(KEY_BOOKING_ID, bookingIntId);
        nextIntent.putExtra(KEY_SNACK_ID, snackId);
        startActivity(nextIntent);

        Toast.makeText(this, "Selected Booking. Proceeding to Snack Selection.", Toast.LENGTH_SHORT).show();
    }

    private List<String[]> getActiveBookingsForUser(int userId) {
        return dbHelper.getActiveBookingsForUser(userId);
    }

    private class BookingAdapter extends RecyclerView.Adapter<BookingViewHolder> {

        private final List<String[]> localBookingList;

        public BookingAdapter(List<String[]> bookingList) {
            this.localBookingList = bookingList;
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_selection, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            String[] bookingData = localBookingList.get(position);
            final String bookingId = bookingData[0];
            final String displayInfo = bookingData[1];

            holder.textViewBookingId.setText(bookingId);
            holder.textViewDisplayInfo.setText(displayInfo);

            holder.bookingItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookingSelectionActivity.this.handleBookingSelection(bookingId);
                }
            });
        }

        @Override
        public int getItemCount() {
            return localBookingList.size();
        }
    }

    private class BookingViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout bookingItemContainer;
        final TextView textViewBookingId;
        final TextView textViewDisplayInfo;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingItemContainer = itemView.findViewById(R.id.booking_item_container);
            textViewBookingId = itemView.findViewById(R.id.textView_booking_id);
            textViewDisplayInfo = itemView.findViewById(R.id.textView_display_info);
        }
    }
}