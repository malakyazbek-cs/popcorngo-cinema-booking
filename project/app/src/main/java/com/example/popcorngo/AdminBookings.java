package com.example.popcorngo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AdminBookings extends AppCompatActivity {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private DatabaseHelper dbHelper;

    private Spinner spinnerStatusFilter;
    private EditText etStartDate;
    private EditText etEndDate;
    private EditText etShowtimeMovie;
    private EditText etUserSearch;
    private Button btnApplyFilters;
    private Button btnClearFilters;
    private ImageButton btnBack;

    private final String[] statusOptions = {"All", "Paid", "Pending", "Cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        dbHelper = new DatabaseHelper(this);

        rvBookings = findViewById(R.id.rv_bookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        spinnerStatusFilter = findViewById(R.id.spinner_status_filter);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etShowtimeMovie = findViewById(R.id.et_showtime_movie);
        etUserSearch = findViewById(R.id.et_user_search);
        btnApplyFilters = findViewById(R.id.btn_apply_filters);
        btnClearFilters = findViewById(R.id.btn_clear_filters);
        btnBack = findViewById(R.id.btn_back);

        setupSpinner();
        setupDatePickers();
        setupFilterButtons();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminBookings.this, AdminDashboard.class);
            startActivity(intent);
            finish();
        });

        loadBookings(null, null, null, null, null);
    }

    private void setupSpinner() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker((EditText) v));
        etEndDate.setOnClickListener(v -> showDatePicker((EditText) v));
    }

    private void showDatePicker(final EditText editText) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.MyDatePickerTheme,
                (view, year1, month1, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    editText.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void setupFilterButtons() {
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        btnClearFilters.setOnClickListener(v -> clearFilters());
    }

    private void applyFilters() {

        String statusFilter = spinnerStatusFilter.getSelectedItem().toString();
        String filterStatus = statusFilter.equals("All") ? null : statusFilter;

        String startDate = etStartDate.getText().toString().isEmpty() ? null : etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString().isEmpty() ? null : etEndDate.getText().toString();
        String movieTitle = etShowtimeMovie.getText().toString().isEmpty() ? null : etShowtimeMovie.getText().toString();
        String userQuery = etUserSearch.getText().toString().isEmpty() ? null : etUserSearch.getText().toString();

        loadBookings(filterStatus, startDate, endDate, movieTitle, userQuery);

        Toast.makeText(this, "Filters Applied", Toast.LENGTH_SHORT).show();
    }

    private void clearFilters() {
        spinnerStatusFilter.setSelection(0);
        etStartDate.setText("");
        etEndDate.setText("");
        etShowtimeMovie.setText("");
        etUserSearch.setText("");

        loadBookings(null, null, null, null, null);

        Toast.makeText(this, "Filters Cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadBookings(String statusFilter, String startDate, String endDate, String movieTitle, String userQuery) {

        Cursor cursor = dbHelper.getDetailedBookings(statusFilter, startDate, endDate, movieTitle, userQuery);
        List<Booking> bookings = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int bookingId = cursor.getInt(cursor.getColumnIndexOrThrow("booking_id"));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email"));
                String mTitle = cursor.getString(cursor.getColumnIndexOrThrow("movie_title"));
                String showtimeDate = cursor.getString(cursor.getColumnIndexOrThrow("showtime_date"));
                String showtimeTime = cursor.getString(cursor.getColumnIndexOrThrow("showtime_time"));
                double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String bookingDate = cursor.getString(cursor.getColumnIndexOrThrow("booking_date"));

                List<String> seats = dbHelper.getSeatsForBooking(bookingId);
                List<String[]> snacks = dbHelper.getSnacksForBooking(bookingId);

                bookings.add(new Booking(bookingId, userEmail, mTitle, showtimeDate, showtimeTime,
                        totalPrice, status, bookingDate, seats, snacks));

            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();

        adapter = new BookingAdapter(bookings);
        rvBookings.setAdapter(adapter);
    }

    static class Booking {
        int id;
        String userEmail, movieTitle, showtimeDate, showtimeTime, status, bookingDate;
        double totalPrice;
        List<String> seats;
        List<String[]> snacks;

        Booking(int id, String userEmail, String movieTitle, String showtimeDate, String showtimeTime,
                double totalPrice, String status, String bookingDate, List<String> seats, List<String[]> snacks) {
            this.id = id;
            this.userEmail = userEmail;
            this.movieTitle = movieTitle;
            this.showtimeDate = showtimeDate;
            this.showtimeTime = showtimeTime;
            this.totalPrice = totalPrice;
            this.status = status;
            this.bookingDate = bookingDate;
            this.seats = seats;
            this.snacks = snacks;
        }
    }

    static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        private final List<Booking> bookings;

        BookingAdapter(List<Booking> bookings) {
            this.bookings = bookings;
        }

        @Override
        public BookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);
            layout.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setBackgroundResource(android.R.color.white);
            return new BookingViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(BookingViewHolder holder, int position) {
            holder.bind(bookings.get(position));
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        static class BookingViewHolder extends RecyclerView.ViewHolder {
            LinearLayout container;

            BookingViewHolder(LinearLayout itemView) {
                super(itemView);
                container = itemView;
            }

            void bind(Booking booking) {
                container.removeAllViews();

                TextView tvUser = new TextView(container.getContext());
                tvUser.setText("User : " + booking.userEmail);
                tvUser.setTextSize(16f);
                container.addView(tvUser);

                TextView tvMovie = new TextView(container.getContext());
                tvMovie.setText("Movie : " + booking.movieTitle);
                container.addView(tvMovie);

                TextView tvShowtime = new TextView(container.getContext());
                tvShowtime.setText("Showtime : " + booking.showtimeDate + " " + booking.showtimeTime);
                container.addView(tvShowtime);

                TextView tvSeats = new TextView(container.getContext());
                tvSeats.setText("Seats : " + String.join(", ", booking.seats));
                container.addView(tvSeats);

                if (!booking.snacks.isEmpty()) {
                    StringBuilder snackText = new StringBuilder("Snacks : ");
                    for (String[] s : booking.snacks) {
                        snackText.append(s[0]).append(" x").append(s[1]).append(", ");
                    }
                    snackText.setLength(snackText.length() - 2);
                    TextView tvSnacks = new TextView(container.getContext());
                    tvSnacks.setText(snackText.toString());
                    container.addView(tvSnacks);
                }

                TextView tvPrice = new TextView(container.getContext());
                tvPrice.setText("Total : $" + String.format(Locale.US, "%.2f", booking.totalPrice));
                container.addView(tvPrice);

                TextView tvStatus = new TextView(container.getContext());
                tvStatus.setText("Status : " + booking.status);
                container.addView(tvStatus);

                TextView tvDate = new TextView(container.getContext());
                tvDate.setText("Booked On : " + booking.bookingDate);
                container.addView(tvDate);

                LinearLayout divider = new LinearLayout(container.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2);
                params.setMargins(0, 20, 0, 20);
                divider.setLayoutParams(params);
                divider.setBackgroundColor(0xFFCCCCCC);
                container.addView(divider);
            }
        }
    }
}