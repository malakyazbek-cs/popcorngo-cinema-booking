package com.example.popcorngo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.*;


public class ShowtimesSeatsFragment extends Fragment {

    private Spinner spinnerMovies;
    private RecyclerView rvShowtimes;
    private FloatingActionButton fabAdd;
    private ShowtimesAdapter adapter;
    private DatabaseHelper db;

    private List<String[]> moviesList = new ArrayList<>();

    public ShowtimesSeatsFragment() {
        super(R.layout.fragment_showtimes);
    }

    public static class Showtime {
        public int id, movieId, screen, booked, total;
        public String dateTime;
        public double price;
        public String movieTitle;

        public Showtime(int id, int movieId, String movieTitle, String date, String time,
                        int screen, double price, int booked, int total) {
            this.id = id;
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.dateTime = date + ", " + time;
            this.screen = screen;
            this.price = price;
            this.booked = booked;
            this.total = total;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCurrentMovie();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_showtimes, container, false);

        db = new DatabaseHelper(getContext());

        spinnerMovies = v.findViewById(R.id.spinnerMovies);
        rvShowtimes = v.findViewById(R.id.rvShowtimes);
        fabAdd = v.findViewById(R.id.fabAddShowtime);

        setupMoviesSpinner();
        setupRecyclerView();

        fabAdd.setOnClickListener(view ->
                startActivity(new Intent(getContext(), AddEditShowtimeActivity.class))
        );

        return v;
    }

    private void setupMoviesSpinner() {
        moviesList = db.getMovieIdsAndTitles();
        List<String> titles = new ArrayList<>();

        for (String[] m : moviesList) titles.add(m[1]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                titles
        );

        spinnerMovies.setAdapter(adapter);

        spinnerMovies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterShowtimes(titles.get(pos));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                clearList();
            }
        });
    }

    private int getMovieId(String title) {
        for (String[] m : moviesList)
            if (m[1].equals(title)) return Integer.parseInt(m[0]);
        return -1;
    }

    private void setupRecyclerView() {
        rvShowtimes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShowtimesAdapter(
                new ArrayList<>(),
                this::onShowtimeClick,
                this::onShowtimeLongClick
        );
        rvShowtimes.setAdapter(adapter);
    }

    private void refreshCurrentMovie() {
        String title = (String) spinnerMovies.getSelectedItem();
        if (title != null) filterShowtimes(title);
    }

    private void clearList() {
        adapter.updateData(new ArrayList<>());
    }

    private void filterShowtimes(String title) {
        int movieId = getMovieId(title);
        if (movieId == -1) { clearList(); return; }

        Cursor c = null;
        List<Showtime> list = new ArrayList<>();

        try {
            c = db.getShowtimesByMovieId(movieId);
            if (c.moveToFirst()) list = readShowtimes(c);
        } catch (Exception e) {
            Log.e("Showtimes", e.getMessage());
        } finally {
            if (c != null) c.close();
        }

        adapter.updateData(list);
    }

    private String getMovieTitleById(int movieId) {
        List<String[]> movies = db.getMovieIdsAndTitles();
        for (String[] m : movies) {
            if (Integer.parseInt(m[0]) == movieId) {
                return m[1];
            }
        }
        return "";
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

                String movieTitle = getMovieTitleById(movieId);

                int[] seats = db.getShowtimeSeatCount(id);
                int bookedSeats = seats[1];
                int totalSeats = seats[0];

                list.add(new Showtime(id, movieId, movieTitle, date, time, screen, price, bookedSeats, totalSeats));

            } while (c.moveToNext());
        }

        return list;
    }

    private void onShowtimeClick(Showtime s) {
        Intent intent = new Intent(getContext(), AddEditShowtimeActivity.class);
        intent.putExtra("showtimeId", s.id);
        startActivity(intent);
    }

    private boolean onShowtimeLongClick(Showtime s) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Showtime")
                .setMessage("Delete Movie " + s.movieTitle + " showing on Screen " + s.screen + " on " + s.dateTime + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteShowtime(s.id);
                    refreshCurrentMovie();
                })
                .setNegativeButton("Cancel", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        return true;
    }

    public static class ShowtimesAdapter extends RecyclerView.Adapter<ShowtimesAdapter.Holder> {

        private List<Showtime> list;
        public interface Click { void onClick(Showtime s); }
        public interface LongClick { boolean onLong(Showtime s); }

        private final Click click;
        private final LongClick longClick;

        public ShowtimesAdapter(List<Showtime> list, Click click, LongClick longClick) {
            this.list = list;
            this.click = click;
            this.longClick = longClick;
        }

        public void updateData(List<Showtime> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_showtime, parent, false);
            return new Holder(v);
        }

        @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
            Showtime s = list.get(pos);
            h.bind(s, click, longClick);
        }

        @Override public int getItemCount() { return list.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvTime, tvScreen, tvPrice, tvSeats;

            public Holder(@NonNull View item) {
                super(item);
                tvTime = item.findViewById(R.id.tvShowtimeDateTime);
                tvScreen = item.findViewById(R.id.tvShowtimeScreen);
                tvPrice = item.findViewById(R.id.tvTicketPrice);
                tvSeats = item.findViewById(R.id.tvSeatsBooked);
            }

            void bind(Showtime s, Click click, LongClick longClick) {
                tvTime.setText(s.dateTime.replace("-", "/"));
                tvScreen.setText("Screen : " + s.screen);
                tvPrice.setText(String.format("Price : $%.2f", s.price));
                tvSeats.setText("Seats : " + s.booked + "/" + s.total);

                itemView.setOnClickListener(v -> click.onClick(s));
                itemView.setOnLongClickListener(v -> longClick.onLong(s));
            }
        }
    }
}
