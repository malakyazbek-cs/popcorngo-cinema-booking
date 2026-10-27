package com.example.popcorngo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class UserTickets extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoBookings;
    private DatabaseHelper dbHelper;
    private static int CURRENT_USER_ID = -1;

    public UserTickets() {}

    public static UserTickets newInstance() {
        return new UserTickets();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_tickets, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_bookings);
        tvNoBookings = view.findViewById(R.id.tv_no_bookings);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof UserDashboard) {
            CURRENT_USER_ID = ((UserDashboard) getActivity()).getUserId();
        }

        loadUserBookings();
    }

    private void loadUserBookings() {
        List<BookingSummary> bookings = dbHelper.getUserBookings(CURRENT_USER_ID);

        if (bookings == null || bookings.isEmpty()) {
            tvNoBookings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Log.d("UserTickets", "No bookings found for user ID: " + CURRENT_USER_ID);
        } else {
            tvNoBookings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            BookingAdapter adapter = new BookingAdapter(getContext(), bookings);
            recyclerView.setAdapter(adapter);
            Log.d("UserTickets", "Loaded " + bookings.size() + " bookings.");
        }
    }

    public static class BookingSummary {
        private final int bookingId;
        private final String movieTitle;
        private final String dateTime;
        private final double totalPrice;
        private final String status;

        public BookingSummary(int bookingId, String movieTitle, String dateTime, double totalPrice, String status) {
            this.bookingId = bookingId;
            this.movieTitle = movieTitle;
            this.dateTime = dateTime;
            this.totalPrice = totalPrice;
            this.status = status;
        }

        public int getBookingId() { return bookingId; }
        public String getMovieTitle() { return movieTitle; }
        public String getDateTime() { return dateTime; }
        public double getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
    }

    public static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        private final Context context;
        private final List<BookingSummary> bookingList;

        public BookingAdapter(Context context, List<BookingSummary> bookingList) {
            this.context = context;
            this.bookingList = bookingList != null ? bookingList : new ArrayList<>();
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_booking_summary, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            BookingSummary currentBooking = bookingList.get(position);

            holder.movieTitle.setText(currentBooking.getMovieTitle());
            holder.dateTime.setText(currentBooking.getDateTime());
            holder.totalPrice.setText(String.format(Locale.getDefault(), "Total: $%.2f", currentBooking.getTotalPrice()));
            holder.status.setText(currentBooking.getStatus().toUpperCase());

            String statusText = currentBooking.getStatus().toLowerCase();
            if (statusText.equals("paid")) {
                holder.status.setTextColor(Color.parseColor("#4CAF50"));
            } else if (statusText.equals("pending")) {
                holder.status.setTextColor(Color.parseColor("#FF9800"));
            } else {
                holder.status.setTextColor(Color.parseColor("#F44336"));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, BookingDetailsActivity.class);
                intent.putExtra("userId", CURRENT_USER_ID);
                intent.putExtra("BOOKING_ID", currentBooking.getBookingId());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return bookingList.size();
        }

        public static class BookingViewHolder extends RecyclerView.ViewHolder {
            TextView movieTitle;
            TextView dateTime;
            TextView totalPrice;
            TextView status;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                movieTitle = itemView.findViewById(R.id.tv_movie_title_summary);
                dateTime = itemView.findViewById(R.id.tv_showtime_datetime_summary);
                totalPrice = itemView.findViewById(R.id.tv_total_price_summary);
                status = itemView.findViewById(R.id.tv_booking_status_summary);
            }
        }
    }
}
