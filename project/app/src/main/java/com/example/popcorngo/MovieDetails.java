package com.example.popcorngo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MovieDetails extends AppCompatActivity {

    private ImageView ivPoster;
    private TextView tvTitle, tvSynopsis, tvDuration, tvGenre, tvTrailerUrl;
    private RecyclerView rvShowtimes;
    private DatabaseHelper dbHelper;
    private int currentMovieId = -1;
    private List<Showtime> showtimesList = new ArrayList<>();
    private int currentUserId = -1;

    public static class Showtime {
        int id;
        int movieId;
        String movieTitle;
        String dateTime;
        int screen;
        double ticketPrice;
        int seatsBooked;
        int totalSeats;

        public Showtime(int id, int movieId, String movieTitle, String date, String time, int screen,
                        double ticketPrice, int seatsBooked, int totalSeats) {
            this.id = id;
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.dateTime = date + ", " + time;
            this.screen = screen;
            this.ticketPrice = ticketPrice;
            this.seatsBooked = seatsBooked;
            this.totalSeats = totalSeats;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ivPoster = findViewById(R.id.iv_poster);
        tvTitle = findViewById(R.id.tv_title);
        tvSynopsis = findViewById(R.id.tv_synopsis);
        tvDuration = findViewById(R.id.tv_duration);
        tvGenre = findViewById(R.id.tv_genre);
        tvTrailerUrl = findViewById(R.id.tv_trailer_url);
        rvShowtimes = findViewById(R.id.rvShowtimes);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        currentMovieId = intent.getIntExtra("MOVIE_ID", -1);
        currentUserId = intent.getIntExtra("USER_ID", -1);

        if (currentMovieId != -1) {
            loadMovieDetails(currentMovieId);
            loadShowtimes(currentMovieId, currentUserId);
        } else {
            Toast.makeText(this, "Error: Movie ID not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentMovieId != -1) {
            loadShowtimes(currentMovieId, currentUserId);
        }
    }

    private void loadMovieDetails(int movieId) {
        String[] movieData = dbHelper.getMovieById(movieId);
        if (movieData != null) {
            tvTitle.setText(movieData[1]);
            tvSynopsis.setText(movieData[2]);
            tvDuration.setText(movieData[3] + " MINUTES");
            tvGenre.setText(movieData[4]);
            int posterResId = getResources().getIdentifier(movieData[5], "drawable", getPackageName());
            if (posterResId != 0) ivPoster.setImageResource(posterResId);
            tvTrailerUrl.setText(movieData[6]);
            tvTrailerUrl.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieData[6]));
                startActivity(browserIntent);
            });
        }
    }

    private void loadShowtimes(int movieId, int userId) {
        Cursor c = dbHelper.getShowtimesByMovieId(movieId);
        showtimesList = readShowtimes(c);
        if (showtimesList.isEmpty()) {
            Toast.makeText(this, "No Showtimes Available", Toast.LENGTH_LONG).show();
            return;
        }
        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        ShowtimeAdapter adapter = new ShowtimeAdapter(this, showtimesList, userId);
        rvShowtimes.setAdapter(adapter);
    }

    private List<Showtime> readShowtimes(Cursor c) {
        List<Showtime> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            int idIndex = c.getColumnIndexOrThrow("showtime_id");
            int movieIdIndex = c.getColumnIndexOrThrow("movie_id");
            int screenIndex = c.getColumnIndexOrThrow("screen_number");
            int dateIndex = c.getColumnIndexOrThrow("date");
            int timeIndex = c.getColumnIndexOrThrow("time");
            int priceIndex = c.getColumnIndexOrThrow("ticket_price");
            do {
                int id = c.getInt(idIndex);
                int movieId = c.getInt(movieIdIndex);
                int screen = c.getInt(screenIndex);
                String date = c.getString(dateIndex);
                String time = c.getString(timeIndex);
                double price = c.getDouble(priceIndex);
                String movieTitle = dbHelper.getMovieTitleById(movieId);
                int[] seats = dbHelper.getShowtimeSeatCount(id);
                int bookedSeats = seats[1];
                int totalSeats = seats[0];
                list.add(new Showtime(id, movieId, movieTitle, date, time, screen, price, bookedSeats, totalSeats));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    private class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
        private final List<Showtime> showtimes;
        private final Context context;
        private final int userId;

        public ShowtimeAdapter(Context context, List<Showtime> showtimes, int userId) {
            this.context = context;
            this.showtimes = showtimes;
            this.userId = userId;
        }

        @Override
        public ShowtimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_showtime, parent, false);
            return new ShowtimeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShowtimeViewHolder holder, int position) {
            Showtime showtime = showtimes.get(position);

            final int availableSeats = showtime.totalSeats - showtime.seatsBooked;
            final boolean isSoldOut = availableSeats <= 0;

            holder.tvDateTime.setText(showtime.dateTime);
            holder.tvScreen.setText(String.format(Locale.getDefault(), "Screen : %d", showtime.screen));
            holder.tvTicketPrice.setText(String.format(Locale.getDefault(), "Price : $%.2f", showtime.ticketPrice));

            holder.tvSeatsBooked.setText(String.format(Locale.getDefault(), "Seats : %d / %d booked",
                    showtime.seatsBooked, showtime.totalSeats));

            int seatsColor = isSoldOut
                    ? ContextCompat.getColor(context, android.R.color.holo_red_dark)
                    : ContextCompat.getColor(context, android.R.color.holo_green_dark);
            holder.tvSeatsBooked.setTextColor(seatsColor);

            int defaultColor = ContextCompat.getColor(context, android.R.color.white);
            int soldOutColor = Color.parseColor("#050505");

            holder.cardViewRoot.setBackgroundColor(isSoldOut ? soldOutColor : defaultColor);

            if (isSoldOut) {
                holder.itemView.setOnClickListener(null);
                holder.itemView.setAlpha(0.6f);
            } else {
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setOnClickListener(v -> {
                    int showtimeId = showtime.id;
                    int movieId = showtime.movieId;

                    Intent bookingIntent = new Intent(context, SeatSelectionActivity.class);
                    bookingIntent.putExtra("SHOWTIME_ID", showtimeId);
                    bookingIntent.putExtra("MOVIE_ID", movieId);
                    bookingIntent.putExtra("USER_ID", this.userId);
                    context.startActivity(bookingIntent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return showtimes.size();
        }

        public class ShowtimeViewHolder extends RecyclerView.ViewHolder {
            final TextView tvDateTime;
            final TextView tvScreen;
            final TextView tvTicketPrice;
            final TextView tvSeatsBooked;
            final CardView cardViewRoot;

            public ShowtimeViewHolder(View itemView) {
                super(itemView);
                tvDateTime = itemView.findViewById(R.id.tvShowtimeDateTime);
                tvScreen = itemView.findViewById(R.id.tvShowtimeScreen);
                tvTicketPrice = itemView.findViewById(R.id.tvTicketPrice);
                tvSeatsBooked = itemView.findViewById(R.id.tvSeatsBooked);
                cardViewRoot = (CardView) itemView;
            }
        }
    }
}