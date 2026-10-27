package com.example.popcorngo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddEditShowtimeActivity extends AppCompatActivity {

    private Spinner spinnerMovies;
    private EditText editTextScreenNumber, editTextDate, editTextTime, editTextTicketPrice;
    private Button buttonCancel, buttonSave;
    private DatabaseHelper dbHelper;
    private List<String[]> movieList;
    private List<Integer> movieIds;
    private int selectedMovieId = -1;
    private int showtimeId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_showtime);

        dbHelper = new DatabaseHelper(this);
        spinnerMovies = findViewById(R.id.spinnerMovies);
        editTextScreenNumber = findViewById(R.id.editTextScreenNumber);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextTicketPrice = findViewById(R.id.editTextTicketPrice);
        buttonCancel = findViewById(R.id.buttonCancelShowtime);
        buttonSave = findViewById(R.id.buttonSaveShowtime);

        if (getIntent() != null && getIntent().hasExtra("showtimeId")) {
            showtimeId = getIntent().getIntExtra("showtimeId", -1);
        }

        loadMovies();

        if (showtimeId > 0) loadShowtimeDetails();

        editTextDate.setOnClickListener(v -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.holo_orange_light));
            }
            showDatePicker();
        });

        editTextTime.setOnClickListener(v -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.holo_orange_light));
            }
            showTimePicker();
        });

        buttonCancel.setOnClickListener(v -> finish());
        buttonSave.setOnClickListener(v -> saveShowtime());
    }

    private void loadMovies() {
        movieList = dbHelper.getMovieIdsAndTitles();
        if (movieList.isEmpty()) {
            Toast.makeText(this, "No movies found. Add movies first.", Toast.LENGTH_SHORT).show();
            return;
        }
        movieIds = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        for (String[] m : movieList) {
            movieIds.add(Integer.parseInt(m[0]));
            titles.add(m[1]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMovies.setAdapter(adapter);
        spinnerMovies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMovieId = movieIds.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMovieId = -1;
            }
        });
    }

    private void loadShowtimeDetails() {
        Cursor cursor = dbHelper.getShowtimeById(showtimeId);
        if (cursor != null && cursor.moveToFirst()) {
            int movieId = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
            int screenNumber = cursor.getInt(cursor.getColumnIndexOrThrow("screen_number"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            double ticketPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("ticket_price"));

            editTextScreenNumber.setText(String.valueOf(screenNumber));
            editTextDate.setText(date);
            editTextTime.setText(time);
            editTextTicketPrice.setText(String.valueOf(ticketPrice));

            int index = movieIds.indexOf(movieId);
            if (index != -1) {
                spinnerMovies.setSelection(index);
                selectedMovieId = movieId;
            }
            cursor.close();
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.MyDatePickerTheme,
                (view, year1, month1, dayOfMonth) ->
                        editTextDate.setText(String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth)),
                year, month, day
        );

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.MyDatePickerTheme,
                (view, hourOfDay, minute1) ->
                        editTextTime.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                hour, minute, true
        );

        timePickerDialog.show();
    }

    private boolean validateShowtimeFields(int screenNumber, String date, String time) {
        if (screenNumber < 1 || screenNumber > 6) {
            Toast.makeText(this, "Screen number must be between 1 and 6.", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            String[] dateParts = date.split("-");
            if (dateParts.length != 3) throw new Exception();
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int day = Integer.parseInt(dateParts[2]);
            Calendar showtimeCal = Calendar.getInstance();
            showtimeCal.set(year, month, day, 0, 0, 0);
            showtimeCal.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (showtimeCal.before(today)) {
                Toast.makeText(this, "Date must be today or in the future.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (showtimeCal.equals(today)) {
                String[] timeParts = time.split(":");
                if (timeParts.length != 2) throw new Exception();
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                Calendar showtimeNow = Calendar.getInstance();
                showtimeNow.set(year, month, day, hour, minute, 0);
                showtimeNow.set(Calendar.MILLISECOND, 0);
                Calendar now = Calendar.getInstance();
                if (showtimeNow.before(now)) {
                    Toast.makeText(this, "Time cannot be before current time.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date or time format.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveShowtime() {
        try {
            if (selectedMovieId == -1) {
                Toast.makeText(this, "Please select a movie.", Toast.LENGTH_SHORT).show();
                return;
            }
            String screenStr = editTextScreenNumber.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            String priceStr = editTextTicketPrice.getText().toString().trim();
            if (screenStr.isEmpty() || date.isEmpty() || time.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            int screenNumber;
            double ticketPrice;
            try {
                screenNumber = Integer.parseInt(screenStr);
                ticketPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validateShowtimeFields(screenNumber, date, time)) return;

            if (showtimeId > 0) {
                int rows = dbHelper.updateShowtime(showtimeId, selectedMovieId, screenNumber, date, time, ticketPrice);
                if (rows > 0) Toast.makeText(this, "Showtime updated.", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Failed to update showtime.", Toast.LENGTH_SHORT).show();
            } else {
                long id = dbHelper.addShowtime(selectedMovieId, screenNumber, date, time, ticketPrice);
                if (id > 0) Toast.makeText(this, "Showtime added.", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Failed to add showtime.", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving showtime.", Toast.LENGTH_SHORT).show();
            Log.e("AddEditShowtime", "Error: " + e.getMessage());
        }
    }
}
