package com.example.popcorngo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "PopcornGo.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    public static final String T_USERS = "User";
    public static final String T_MOVIES = "Movie";
    public static final String T_SHOWTIMES = "Showtime";
    public static final String T_SEAT = "Seat";
    public static final String T_SNACK = "Snack";
    public static final String T_BOOKING = "Booking";
    public static final String T_BOOKINGSNACK = "Booking_Snack";
    public static final String T_BOOKINGSEAT = "Booking_Seat";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // =========================
        // TABLES
        // =========================

        // USER TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "balance REAL DEFAULT 0.0, " +
                "credit_card TEXT)");

        // MOVIE TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_MOVIES + " (" +
                "movie_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "synopsis TEXT, " +
                "duration INTEGER, " +
                "genre TEXT, " +
                "poster_url TEXT, " +
                "trailer_url TEXT, " +
                "status TEXT)");

        // SHOWTIME TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_SHOWTIMES + " (" +
                "showtime_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, " +
                "screen_number INTEGER, " +
                "date TEXT, " +
                "time TEXT, " +
                "ticket_price REAL, " +
                "showtime_datetime TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES " + T_MOVIES + "(movie_id) ON DELETE CASCADE, " +
                "UNIQUE(date, time, screen_number))");

        // SEAT TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_SEAT + " (" +
                "seat_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "showtime_id INTEGER, " +
                "seat_number TEXT, " +
                "status TEXT DEFAULT 'Available', " +
                "FOREIGN KEY(showtime_id) REFERENCES " + T_SHOWTIMES + "(showtime_id) ON DELETE CASCADE)");

        // SNACK TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_SNACK + " (" +
                "snack_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "price REAL, " +
                "stock INTEGER DEFAULT 0, " +
                "image_url TEXT)");

        // BOOKING TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_BOOKING + " (" +
                "booking_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "showtime_id INTEGER, " +
                "total_price REAL, " +
                "booking_date TEXT, " +
                "status TEXT DEFAULT 'Pending', " +
                "FOREIGN KEY(user_id) REFERENCES " + T_USERS + "(user_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(showtime_id) REFERENCES " + T_SHOWTIMES + "(showtime_id) ON DELETE CASCADE)");

        // BOOKING_SNACK TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_BOOKINGSNACK + " (" +
                "booking_snack_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER, " +
                "snack_id INTEGER, " +
                "quantity INTEGER, " +
                "FOREIGN KEY(booking_id) REFERENCES " + T_BOOKING + "(booking_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(snack_id) REFERENCES " + T_SNACK + "(snack_id) ON DELETE CASCADE)");

        // BOOKING_SEAT TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_BOOKINGSEAT + " (" +
                "booking_seat_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER, " +
                "seat_id INTEGER, " +
                "FOREIGN KEY(booking_id) REFERENCES " + T_BOOKING + "(booking_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(seat_id) REFERENCES " + T_SEAT + "(seat_id) ON DELETE CASCADE)");


        // =======================
        // SAMPLE DATA INSERTS
        // =======================

        // USERS
        db.execSQL("INSERT INTO " + T_USERS + " (email, password, balance, credit_card) VALUES " +
                "('Haneen', '1234h', 50.00, '1234'), " +
                "('Israa', '1111i', 30.00, '5678'), " +
                "('Ali', '5555a', 100.00, '9999')");

        // MOVIES
        db.execSQL("INSERT INTO " + T_MOVIES + " (title, synopsis, duration, genre, poster_url, trailer_url, status) VALUES " +
                "('Attack on Titan', 'The fate of humanity rests on Eren and his allies as Titans and men collide for the last time.', 145, 'Action / Fantasy', 'aot_final', 'https://youtu.be/MGRm4IzK1SQ', 'SHOWING_NOW')," +
                "('Inside Out 2', 'Riley faces new emotions as she enters her teenage years.', 96, 'Animation', 'insideout2', 'https://youtu.be/LEjhY15eCx0', 'SHOWING_NOW')," +
                "('The Dark Tower', 'Roland Deschain embarks on a new quest through worlds and timelines to protect the Dark Tower.', 135, 'Fantasy / Sci-Fi', 'darktower2', 'https://youtu.be/GjwfqXTebIY', 'SHOWING_NOW')," +
                "('Deadpool & Wolverine', 'Two antiheroes team up for chaos, comedy, and carnage.', 127, 'Action', 'deadpool', 'https://youtu.be/IDmQ7a1FZlw', 'SHOWING_NOW')," +
                "('Kung Fu Panda 4', 'Po takes on a new villain while training his successor.', 95, 'Animation', 'kfp4', 'https://youtu.be/_inKs4eeHiI', 'SHOWING_NOW')," +
                "('Avatar : The Way of Water', 'Jake Sully and Neytiri fight to protect their family in Pandora’s oceans.', 192, 'Sci-Fi', 'avatar2', 'https://youtu.be/d9MyW72ELq0', 'COMING_SOON')," +
                "('Spider-Man', 'Miles Morales faces new multiverse challenges.', 150, 'Animation', 'spiderman3', 'https://youtu.be/cqGjhVJWtEg', 'OLD')," +
                "('Despicable Me 4', 'Gru and his minions are back for more fun chaos.', 90, 'Animation', 'dm4', 'https://youtu.be/6DxjJzmYsXo', 'OLD')," +
                "('Joker', 'Arthur Fleck returns as the Joker in a haunting musical sequel.', 138, 'Thriller', 'joker2', 'https://youtu.be/fVdYd3WzFYw', 'OLD')," +
                "('Venom : The Last Dance', 'Eddie Brock and Venom face their ultimate showdown.', 110, 'Action', 'venom3', 'https://youtu.be/yy9zQsmz3eE', 'COMING_SOON')," +
                "('Wish', 'Asha makes a wish so powerful it’s answered by a cosmic force.', 95, 'Fantasy', 'wish', 'https://youtu.be/oyRxxpD3yNw', 'COMING_SOON')," +
                "('Demon Slayer : Infinity Castle', 'Tanjiro and his friends face Muzan Kibutsuji in the final battle within the Infinity Castle.', 120, 'Anime / Action', 'demonslayer_castle', 'https://youtu.be/fFz2R7Zx5fE', 'COMING_SOON')," +
                "('Jumanji 4', 'The group returns to the game with new characters, new powers, and even more dangerous challenges.', 132, 'Adventure / Comedy', 'jumanji4', 'https://youtu.be/2QKg5SZ_35I', 'COMING_SOON')," +
                "('Dune : Part Two', 'Paul Atreides unites with the Fremen to avenge his family and bring peace to Arrakis.', 166, 'Sci-Fi', 'dune2', 'https://youtu.be/_YUzQa_1RCE', 'COMING_SOON')");

        // SNACKS
        db.execSQL("INSERT INTO " + T_SNACK + " (name, price, stock, image_url) VALUES " +
                "('Classic Popcorn', 4.50, 14, 'classicpopcorn')," +
                "('Caramel Popcorn Tub', 6.00, 9, 'caramelpopcorn')," +
                "('Vanilla Milkshake', 5.50, 12, 'vanillamilkshake')," +
                "('Chocolate Milkshake', 5.50, 4, 'chocolatemilkshake')," +
                "('Ice Cream Cup ( Strawberry )', 3.00, 10, 'icecreamcup')," +
                "('Nachos with Cheese Dip', 5.50, 12, 'nachoscheesedip')," +
                "('Hot Dog ( Beef )', 7.00, 8, 'hotdog')," +
                "('Chili Cheese Fries', 6.50, 11, 'chilicheesefries')," +
                "('Soft Drink ( Large )', 3.00, 13, 'softdrink')," +
                "('Bottled Water', 2.00, 10, 'bottledwater')," +
                "('Chocolate Bar', 2.50, 6, 'chocolatebar')," +
                "('Slushie ( Blue Raspberry )', 4.00, 7, 'slushie')," +
                "('Iced Coffee', 3.50, 9, 'icedcoffee')," +
                "('Combo Meal ( Popcorn + Drink )', 6.50, 8, 'combomeal')," +
                "('Mini Donuts', 4.25, 3, 'minidonuts')," +
                "('Energy Drink', 4.50, 7, 'energydrink')");



        db.execSQL("INSERT INTO " + T_SHOWTIMES + " (movie_id, screen_number, date, time, ticket_price, showtime_datetime) VALUES " +
                "(1, 1, '2025-11-21', '18:00', 12.00, '2025-11-21 18:00:00')," +
                "(2, 2, '2025-11-21', '19:30', 10.00, '2025-11-21 19:30:00')," +
                "(3, 3, '2025-11-22', '20:45', 15.00, '2025-11-22 20:45:00')," +
                "(4, 1, '2025-11-23', '17:00', 10.00, '2025-11-23 17:00:00')," +
                "(5, 2, '2025-11-24', '21:00', 15.00, '2025-11-24 21:00:00')");

        db.execSQL("INSERT INTO " + T_SEAT + " (showtime_id, seat_number, status) VALUES " +
                "(1, 'A', 'Available'), (1, 'B', 'Available'), (1, 'C', 'Available'), (1, 'D', 'Available'), (1, 'E', 'Available')," +
                "(2, 'A', 'Available'), (2, 'B', 'Available'), (2, 'C', 'Available'), (2, 'D', 'Available'), (2, 'E', 'Available')," +
                "(3, 'A', 'Available'), (3, 'B', 'Available'), (3, 'C', 'Available'), (3, 'D', 'Available'), (3, 'E', 'Available')," +
                "(4, 'A', 'Available'), (4, 'B', 'Available'), (4, 'C', 'Available'), (4, 'D', 'Available'), (4, 'E', 'Available')," +
                "(5, 'A', 'Available'), (5, 'B', 'Available'), (5, 'C', 'Available'), (5, 'D', 'Available'), (5, 'E', 'Available')");

        db.execSQL("INSERT INTO " + T_BOOKING + " (user_id, showtime_id, total_price, booking_date, status) VALUES " +
                "(1, 1, 34.50, '2025-11-20 14:30', 'Paid')," +
                "(2, 3, 26.00, '2025-11-20 16:10', 'Paid')," +
                "(3, 2, 14.50, '2025-11-20 17:45', 'Pending')," +
                "(2, 2, 10.00, '2025-11-20 18:00', 'Pending')");



        db.beginTransaction();
        try {
            Cursor seatCursor = db.rawQuery("SELECT seat_id FROM " + T_SEAT + " WHERE showtime_id = 1 AND seat_number IN ('A', 'C')", null);
            if (seatCursor.moveToFirst()) {
                db.execSQL("INSERT INTO " + T_BOOKINGSEAT + " (booking_id, seat_id) VALUES (1, " + seatCursor.getInt(0) + ")");
                db.execSQL("UPDATE " + T_SEAT + " SET status = 'Booked' WHERE seat_id = " + seatCursor.getInt(0));
                if (seatCursor.moveToNext()) {
                    db.execSQL("INSERT INTO " + T_BOOKINGSEAT + " (booking_id, seat_id) VALUES (1, " + seatCursor.getInt(0) + ")");
                    db.execSQL("UPDATE " + T_SEAT + " SET status = 'Booked' WHERE seat_id = " + seatCursor.getInt(0));
                }
            }
            seatCursor.close();

            seatCursor = db.rawQuery("SELECT seat_id FROM " + T_SEAT + " WHERE showtime_id = 3 AND seat_number = 'D'", null);
            if (seatCursor.moveToFirst()) {
                db.execSQL("INSERT INTO " + T_BOOKINGSEAT + " (booking_id, seat_id) VALUES (2, " + seatCursor.getInt(0) + ")");
                db.execSQL("UPDATE " + T_SEAT + " SET status = 'Booked' WHERE seat_id = " + seatCursor.getInt(0));
            }
            seatCursor.close();

            seatCursor = db.rawQuery("SELECT seat_id FROM " + T_SEAT + " WHERE showtime_id = 2 AND seat_number = 'B'", null);
            if (seatCursor.moveToFirst()) {
                db.execSQL("INSERT INTO " + T_BOOKINGSEAT + " (booking_id, seat_id) VALUES (3, " + seatCursor.getInt(0) + ")");
                db.execSQL("UPDATE " + T_SEAT + " SET status = 'Booked' WHERE seat_id = " + seatCursor.getInt(0));
            }
            seatCursor.close();

            seatCursor = db.rawQuery("SELECT seat_id FROM " + T_SEAT + " WHERE showtime_id = 2 AND seat_number = 'A'", null);
            if (seatCursor.moveToFirst()) {
                int seatId = seatCursor.getInt(0);
                db.execSQL("INSERT INTO " + T_BOOKINGSEAT + " (booking_id, seat_id) VALUES (4, " + seatId + ")");
                db.execSQL("UPDATE " + T_SEAT + " SET status = 'Booked' WHERE seat_id = " + seatId);
            }
            seatCursor.close();

            db.execSQL("INSERT INTO " + T_BOOKINGSNACK + " (booking_id, snack_id, quantity) VALUES " +
                    "(1, 1, 1), " +
                    "(1, 2, 1), " +
                    "(2, 3, 2), " +
                    "(3, 1, 1)");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + T_BOOKINGSEAT);
            db.execSQL("DROP TABLE IF EXISTS " + T_BOOKINGSNACK);
            db.execSQL("DROP TABLE IF EXISTS " + T_BOOKING);
            db.execSQL("DROP TABLE IF EXISTS " + T_SEAT);
            db.execSQL("DROP TABLE IF EXISTS " + T_SHOWTIMES);
            db.execSQL("DROP TABLE IF EXISTS " + T_SNACK);
            db.execSQL("DROP TABLE IF EXISTS " + T_MOVIES);
            db.execSQL("DROP TABLE IF EXISTS " + T_USERS);

            onCreate(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }



    // ======================
    // METHODS
    // ======================

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT user_id FROM " + T_USERS + " WHERE LOWER(email)=LOWER(?)",
                new String[]{email.trim()}
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email.trim());
        cv.put("password", password);
        long id = -1;
        try {
            id = db.insertOrThrow(T_USERS, null, cv);
        } catch (SQLException e) {
            return false;
        }
        return id > 0;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT user_id FROM " + T_USERS + " WHERE LOWER(email)=LOWER(?) AND password=?",
                new String[]{email.trim(), password}
        );
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public int getUserId(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT user_id FROM " + T_USERS + " WHERE LOWER(email)=LOWER(?)", new String[]{email.trim()});
        int id = -1;
        if (c.moveToFirst())
            id = c.getInt(0);
        c.close();
        return id;
    }

    public int getTotalMovies() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) AS total_movies FROM " + T_MOVIES, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total_movies"));
        }
        cursor.close();
        return total;
    }

    public int getBookingsToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) AS bookings_today FROM " + T_BOOKING + " WHERE DATE(booking_date, 'localtime') = DATE('now', 'localtime')", null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("bookings_today"));
        }
        cursor.close();
        return total;
    }

    public int getBookingsThisWeek() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) AS bookings_week FROM " + T_BOOKING + " WHERE DATE(booking_date, 'localtime') >= DATE('now', 'weekday 0', '-6 days')", null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("bookings_week"));
        }
        cursor.close();
        return total;
    }

    public double getSeatsOccupancy() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor totalSeatsCursor = db.rawQuery("SELECT COUNT(*) FROM " + T_SEAT, null);
        int totalSeats = 0;
        if (totalSeatsCursor.moveToFirst()) {
            totalSeats = totalSeatsCursor.getInt(0);
        }
        totalSeatsCursor.close();

        if (totalSeats == 0) return 0.0;

        String occupancyQuery =
                "SELECT COUNT(T1.seat_id) AS booked_seats " +
                        "FROM " + T_BOOKINGSEAT + " T1 " +
                        "INNER JOIN " + T_BOOKING + " T2 ON T1.booking_id = T2.booking_id " +
                        "WHERE T2.status = 'Paid'";

        Cursor bookedSeatsCursor = db.rawQuery(occupancyQuery, null);
        int bookedSeats = 0;
        if (bookedSeatsCursor.moveToFirst()) {
            bookedSeats = bookedSeatsCursor.getInt(bookedSeatsCursor.getColumnIndexOrThrow("booked_seats"));
        }
        bookedSeatsCursor.close();

        double occupancy = ((double) bookedSeats * 100.0) / totalSeats;
        return Math.round(occupancy * 100.0) / 100.0;
    }

    public List<String> getLowStockSnacks(int threshold) {
        List<String> snacks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM " + T_SNACK + " WHERE stock < ?", new String[]{String.valueOf(threshold)});
        while (cursor.moveToNext()) {
            snacks.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        return snacks;
    }



    // ==================================
    // MOVIE CRUD OPERATIONS
    // ==================================

    public long addMovie(String title, String synopsis, int duration, String genre, String posterUrl, String trailerUrl, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("synopsis", synopsis);
        cv.put("duration", duration);
        cv.put("genre", genre);
        cv.put("poster_url", posterUrl);
        cv.put("trailer_url", trailerUrl);
        cv.put("status", status);

        long id = -1;
        try {
            id = db.insertOrThrow(T_MOVIES, null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public List<String[]> getMovieIdsAndTitles() {
        List<String[]> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String SELECT_QUERY = "SELECT movie_id, title FROM " + T_MOVIES;
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                String[] movieData = new String[2];
                movieData[0] = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("movie_id")));
                movieData[1] = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                movies.add(movieData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public List<String[]> getAllMovies() {
        List<String[]> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String SELECT_QUERY = "SELECT * FROM " + T_MOVIES;
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                String[] movieData = new String[8];
                movieData[0] = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("movie_id")));
                movieData[1] = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                movieData[2] = cursor.getString(cursor.getColumnIndexOrThrow("synopsis"));
                movieData[3] = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("duration")));
                movieData[4] = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                movieData[5] = cursor.getString(cursor.getColumnIndexOrThrow("poster_url"));
                movieData[6] = cursor.getString(cursor.getColumnIndexOrThrow("trailer_url"));
                movieData[7] = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                movies.add(movieData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public int updateMovie(int movieId, String title, String synopsis, int duration, String genre, String posterUrl, String trailerUrl, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("synopsis", synopsis);
        cv.put("duration", duration);
        cv.put("genre", genre);
        cv.put("poster_url", posterUrl);
        cv.put("trailer_url", trailerUrl);
        cv.put("status", status);

        return db.update(T_MOVIES, cv, "movie_id = ?", new String[]{String.valueOf(movieId)});
    }

    public int updateMovieStatus(int movieId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);

        return db.update(T_MOVIES, cv, "movie_id = ?", new String[]{String.valueOf(movieId)});
    }

    public int deleteMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(T_MOVIES, "movie_id = ?", new String[]{String.valueOf(movieId)});
    }

    public String getMovieTitleById(int movieId) {
        List<String[]> movies = getMovieIdsAndTitles();
        for (String[] m : movies) {
            if (Integer.parseInt(m[0]) == movieId) {
                return m[1];
            }
        }
        return "";
    }

    public String[] getMovieById(int movieId) {
        String[] movieData = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String SELECT_QUERY = "SELECT * FROM " + T_MOVIES + " WHERE movie_id = ?";
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[]{String.valueOf(movieId)});

        if (cursor.moveToFirst()) {
            movieData = new String[8];
            movieData[0] = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("movie_id")));
            movieData[1] = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            movieData[2] = cursor.getString(cursor.getColumnIndexOrThrow("synopsis"));
            movieData[3] = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("duration")));
            movieData[4] = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
            movieData[5] = cursor.getString(cursor.getColumnIndexOrThrow("poster_url"));
            movieData[6] = cursor.getString(cursor.getColumnIndexOrThrow("trailer_url"));
            movieData[7] = cursor.getString(cursor.getColumnIndexOrThrow("status"));
        }
        cursor.close();
        return movieData;
    }



    // ==================================
    // SHOWTIME CRUD METHODS
    // ==================================

    private void createDefaultSeatsForShowtime(long showtimeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] seatLetters = {"A", "B", "C", "D", "E"};

        for (String seatNumber : seatLetters) {
            ContentValues values = new ContentValues();
            values.put("showtime_id", showtimeId);
            values.put("seat_number", seatNumber);
            values.put("status", "Available");

            try {
                db.insertOrThrow(T_SEAT, null, values);
            } catch (SQLException e) {
                Log.e("DatabaseHelper", "Failed to insert seat " + seatNumber + " for showtime ID " + showtimeId + ": " + e.getMessage());
            }
        }
    }

    public long addShowtime(int movieId, int screenNumber, String date, String time, double ticketPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        long showtimeId = -1;
        db.beginTransaction();
        try {
            String showtimeDateTime = date + " " + time + ":00";

            ContentValues values = new ContentValues();
            values.put("movie_id", movieId);
            values.put("screen_number", screenNumber);
            values.put("date", date);
            values.put("time", time);
            values.put("ticket_price", ticketPrice);
            values.put("showtime_datetime", showtimeDateTime);

            showtimeId = db.insertOrThrow(T_SHOWTIMES, null, values);

            if (showtimeId > 0) {
                createDefaultSeatsForShowtime(showtimeId);
                updateMovieStatus(movieId, "SHOWING_NOW");
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "addShowtime failed: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        return showtimeId;
    }

    public int[] getShowtimeSeatCount(int showtimeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] counts = new int[]{0, 0};
        Cursor totalCursor = null;
        Cursor bookedCursor = null;
        try {
            totalCursor = db.rawQuery(
                    "SELECT COUNT(seat_id) AS total FROM " + T_SEAT + " WHERE showtime_id = ?",
                    new String[]{String.valueOf(showtimeId)});
            if (totalCursor.moveToFirst()) {
                counts[0] = totalCursor.getInt(totalCursor.getColumnIndexOrThrow("total"));
            }
            bookedCursor = db.rawQuery(
                    "SELECT COUNT(seat_id) AS booked FROM " + T_SEAT + " WHERE showtime_id = ? AND status = 'Booked'",
                    new String[]{String.valueOf(showtimeId)});
            if (bookedCursor.moveToFirst()) {
                counts[1] = bookedCursor.getInt(bookedCursor.getColumnIndexOrThrow("booked"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getShowtimeSeatCount failed: " + e.getMessage());
        } finally {
            if (totalCursor != null) totalCursor.close();
            if (bookedCursor != null) bookedCursor.close();
        }
        return counts;
    }

    public void deleteOldShowtimesAndUpdateStatus() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            String updateOldMoviesQuery =
                    "UPDATE " + T_MOVIES +
                            " SET status = 'OLD' " +
                            " WHERE status = 'SHOWING_NOW' AND movie_id NOT IN (" +
                            " SELECT DISTINCT movie_id FROM " + T_SHOWTIMES +
                            " WHERE showtime_datetime > datetime('now', 'localtime')" +
                            ");";

            db.execSQL(updateOldMoviesQuery);

            String deleteShowtimesQuery =
                    "DELETE FROM " + T_SHOWTIMES +
                            " WHERE showtime_datetime <= datetime('now', 'localtime');";

            db.execSQL(deleteShowtimesQuery);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "Cleanup failed: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getShowtimeById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + T_SHOWTIMES + " WHERE showtime_id = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getShowtimesByMovieId(int movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + T_SHOWTIMES + " WHERE movie_id = ? ORDER BY date, time", new String[]{String.valueOf(movieId)});
    }

    public int updateShowtime(int id, int movieId, int screenNumber, String date, String time, double ticketPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("movie_id", movieId);
        values.put("screen_number", screenNumber);
        values.put("date", date);
        values.put("time", time);
        values.put("ticket_price", ticketPrice);
        int rows = db.update(T_SHOWTIMES, values, "showtime_id = ?", new String[]{String.valueOf(id)});
        return rows;
    }

    public int deleteShowtime(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(T_SHOWTIMES, "showtime_id = ?", new String[]{String.valueOf(id)});
        return rows;
    }



    // ==================================
    // Snacks CRUD METHODS
    // ==================================

    public boolean addSnack(String name, double price, int stock, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("price", price);
        cv.put("stock", stock);
        cv.put("image_url", imageUrl);
        long res = db.insert(T_SNACK, null, cv);
        return res != -1;
    }

    public boolean updateSnack(int id, String name, double price, int stock, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("price", price);
        cv.put("stock", stock);
        cv.put("image_url", imageUrl);
        int res = db.update(T_SNACK, cv, "snack_id=?", new String[]{String.valueOf(id)});
        return res > 0;
    }

    public boolean deleteSnack(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete(T_SNACK, "snack_id=?", new String[]{String.valueOf(id)});
        return res > 0;
    }

    public Snack getSnackById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_SNACK + " WHERE snack_id=?", new String[]{String.valueOf(id)});
        if (c != null && c.moveToFirst()) {
            Snack snack = new Snack(
                    c.getInt(c.getColumnIndexOrThrow("snack_id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("price")),
                    c.getInt(c.getColumnIndexOrThrow("stock")),
                    c.getString(c.getColumnIndexOrThrow("image_url"))
            );
            c.close();
            return snack;
        }
        if (c != null) c.close();
        return null;
    }

    public List<Snack> getAllSnacks() {
        List<Snack> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_SNACK, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Snack(
                        c.getInt(c.getColumnIndexOrThrow("snack_id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("price")),
                        c.getInt(c.getColumnIndexOrThrow("stock")),
                        c.getString(c.getColumnIndexOrThrow("image_url"))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public Cursor getDetailedBookings(String statusFilter, String startDate, String endDate,
                                      String movieTitle, String userQuery) {

        SQLiteDatabase db = this.getReadableDatabase();
        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder query = new StringBuilder("SELECT ");
        query.append("B.booking_id, U.email AS user_email, M.title AS movie_title, ");
        query.append("S.date AS showtime_date, S.time AS showtime_time, ");
        query.append("B.total_price, B.status, B.booking_date ");
        query.append("FROM ").append(T_BOOKING).append(" B ");
        query.append("INNER JOIN ").append(T_USERS).append(" U ON B.user_id = U.user_id ");
        query.append("INNER JOIN ").append(T_SHOWTIMES).append(" S ON B.showtime_id = S.showtime_id ");
        query.append("INNER JOIN ").append(T_MOVIES).append(" M ON S.movie_id = M.movie_id ");

        List<String> whereClauses = new ArrayList<>();

        if (statusFilter != null && !statusFilter.isEmpty()) {
            whereClauses.add("B.status = ?");
            selectionArgsList.add(statusFilter);
        }

        if (startDate != null && !startDate.isEmpty()) {
            whereClauses.add("DATE(B.booking_date) >= DATE(?)");
            selectionArgsList.add(startDate);
        }

        if (endDate != null && !endDate.isEmpty()) {
            whereClauses.add("DATE(B.booking_date) <= DATE(?)");
            selectionArgsList.add(endDate);
        }

        if (movieTitle != null && !movieTitle.isEmpty()) {
            whereClauses.add("M.title LIKE ?");
            selectionArgsList.add("%" + movieTitle + "%");
        }

        if (userQuery != null && !userQuery.isEmpty()) {
            whereClauses.add("U.email LIKE ?");
            selectionArgsList.add("%" + userQuery + "%");
        }

        if (!whereClauses.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        query.append(" ORDER BY B.booking_date DESC, S.date DESC, S.time DESC");

        return db.rawQuery(query.toString(), selectionArgsList.toArray(new String[0]));
    }

    public List<String> getSeatsForBooking(int bookingId) {
        List<String> seats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT T_SEAT.seat_number " +
                "FROM " + T_BOOKINGSEAT + " " +
                "INNER JOIN " + T_SEAT + " T_SEAT ON " + T_BOOKINGSEAT + ".seat_id = T_SEAT.seat_id " +
                "WHERE " + T_BOOKINGSEAT + ".booking_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(bookingId)});

        if (cursor.moveToFirst()) {
            do {
                seats.add(cursor.getString(cursor.getColumnIndexOrThrow("seat_number")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return seats;
    }

    public List<String[]> getSnacksForBooking(int bookingId) {
        List<String[]> snacks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT T_SNACK.name AS snack_name, " + T_BOOKINGSNACK + ".quantity " +
                "FROM " + T_BOOKINGSNACK + " " +
                "INNER JOIN " + T_SNACK + " T_SNACK ON " + T_BOOKINGSNACK + ".snack_id = T_SNACK.snack_id " +
                "WHERE " + T_BOOKINGSNACK + ".booking_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(bookingId)});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("snack_name"));
                String quantity = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                snacks.add(new String[]{name, quantity});
            } while (cursor.moveToNext());
        }
        cursor.close();
        return snacks;
    }

    public Cursor getBookingsByMovie(boolean mostPopular) {
        SQLiteDatabase db = this.getReadableDatabase();
        String order = mostPopular ? "DESC" : "ASC";
        String query = "SELECT M.title, COUNT(B.booking_id) AS total_bookings " +
                "FROM " + T_BOOKING + " B " +
                "JOIN " + T_SHOWTIMES + " S ON B.showtime_id = S.showtime_id " +
                "JOIN " + T_MOVIES + " M ON S.movie_id = M.movie_id " +
                "GROUP BY M.movie_id " +
                "ORDER BY total_bookings " + order;
        return db.rawQuery(query, null);
    }

    public Cursor getRevenueOverTime(String period) {
        SQLiteDatabase db = this.getReadableDatabase();
        String groupBy;
        switch (period.toLowerCase()) {
            case "week":
                groupBy = "strftime('%W', booking_date)";
                break;
            case "month":
                groupBy = "strftime('%m', booking_date)";
                break;
            default:
                groupBy = "booking_date";
        }
        String query = "SELECT " + groupBy + " AS period, SUM(total_price) AS revenue " +
                "FROM " + T_BOOKING + " " +
                "WHERE status = 'Paid' " +
                "GROUP BY " + groupBy + " " +
                "ORDER BY period";
        return db.rawQuery(query, null);
    }

    public Cursor getTopSellingSnacks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT S.name, SUM(BS.quantity) AS total_sold " +
                "FROM " + T_BOOKINGSNACK + " BS " +
                "JOIN " + T_SNACK + " S ON BS.snack_id = S.snack_id " +
                "GROUP BY S.snack_id " +
                "ORDER BY total_sold DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getMoviesCursorByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + T_MOVIES + " WHERE status = ? ORDER BY title";
        return db.rawQuery(query, new String[]{status});
    }



    // ==================================
    // SEAT OPERATIONS
    // ==================================

    public List<String[]> getAvailableSeatsForShowtime(int showtimeId) {
        List<String[]> availableSeats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT seat_id, seat_number FROM " + T_SEAT +
                " WHERE showtime_id = ? AND status = 'Available' " +
                "ORDER BY seat_number ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(showtimeId)});

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow("seat_id");
            int numberIndex = cursor.getColumnIndexOrThrow("seat_number");
            do {
                String[] seatData = new String[2];
                seatData[0] = String.valueOf(cursor.getInt(idIndex));
                seatData[1] = cursor.getString(numberIndex);
                availableSeats.add(seatData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return availableSeats;
    }

    public double getTicketPriceForShowtime(int showtimeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double price = 0.0;
        Cursor cursor = null;
        try {
            String query = "SELECT ticket_price FROM " + T_SHOWTIMES + " WHERE showtime_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(showtimeId)});
            if (cursor.moveToFirst()) {
                price = cursor.getDouble(cursor.getColumnIndexOrThrow("ticket_price"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getTicketPriceForShowtime failed: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return price;
    }

    public long insertBookingSeat(int bookingId, int showtimeId, String seatNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        db.beginTransaction();
        try {
            Cursor seatCursor = db.rawQuery(
                    "SELECT seat_id FROM " + T_SEAT +
                            " WHERE showtime_id = ? AND seat_number = ? AND status = 'Available'",
                    new String[]{String.valueOf(showtimeId), seatNumber});

            if (seatCursor.moveToFirst()) {
                int seatId = seatCursor.getInt(0);

                ContentValues bookingSeatValues = new ContentValues();
                bookingSeatValues.put("booking_id", bookingId);
                bookingSeatValues.put("seat_id", seatId);
                result = db.insertOrThrow(T_BOOKINGSEAT, null, bookingSeatValues);

                if (result > 0) {
                    ContentValues seatStatusValues = new ContentValues();
                    seatStatusValues.put("status", "Booked");

                    int rowsUpdated = db.update(
                            T_SEAT,
                            seatStatusValues,
                            "seat_id = ?",
                            new String[]{String.valueOf(seatId)}
                    );

                    if (rowsUpdated > 0) {
                        db.setTransactionSuccessful();
                    } else {
                        Log.e("DatabaseHelper", "Failed to update seat status for seat ID: " + seatId);
                        result = -1;
                    }
                }
            } else {
                Log.e("DatabaseHelper", "Seat not available: " + seatNumber + " for showtime " + showtimeId);
            }
            seatCursor.close();

        } catch (SQLException e) {
            Log.e("DatabaseHelper", "Transaction failed during insertBookingSeat: " + e.getMessage());
            result = -1;
        } finally {
            db.endTransaction();
        }
        return result;
    }

    public long insertBooking(int userId, int showtimeId, double totalPrice, String bookingDate, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user_id", userId);
        values.put("showtime_id", showtimeId);
        values.put("total_price", totalPrice);
        values.put("booking_date", bookingDate);
        values.put("status", status);

        long bookingId = db.insert(T_BOOKING, null, values);

        if (bookingId == -1) {
            Log.e("DatabaseHelper", "Failed to insert new booking.");
        }
        return bookingId;
    }



    public List<String[]> getActiveBookingsForUser(int userId) {
        List<String[]> activeBookings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                "B.booking_id, " +
                "M.title, " +
                "S.date, " +
                "S.time " +
                "FROM " + T_BOOKING + " B " +
                "INNER JOIN " + T_SHOWTIMES + " S ON B.showtime_id = S.showtime_id " +
                "INNER JOIN " + T_MOVIES + " M ON S.movie_id = M.movie_id " +
                "WHERE B.user_id = ? " +
                "  AND B.status = 'Pending' " +
                "  AND S.showtime_datetime > datetime('now', 'localtime') " +
                "ORDER BY S.date ASC, S.time ASC";

        String[] selectionArgs = new String[]{String.valueOf(userId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            int bookingIdIndex = cursor.getColumnIndexOrThrow("booking_id");
            int titleIndex = cursor.getColumnIndexOrThrow("title");
            int dateIndex = cursor.getColumnIndexOrThrow("date");
            int timeIndex = cursor.getColumnIndexOrThrow("time");

            do {
                String bookingId = String.valueOf(cursor.getInt(bookingIdIndex));
                String movieTitle = cursor.getString(titleIndex);
                String showtimeDate = cursor.getString(dateIndex);
                String showtimeTime = cursor.getString(timeIndex);

                String displayInfo = movieTitle + " ( " + showtimeDate + " @ " + showtimeTime.substring(0, 5) + " )";
                activeBookings.add(new String[]{bookingId, displayInfo});
            } while (cursor.moveToNext());
        }
        cursor.close();
        return activeBookings;
    }

    public boolean insertBookingSnack(int bookingId, int snackId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("booking_id", bookingId);
        cv.put("snack_id", snackId);
        cv.put("quantity", quantity);

        long res = -1;
        db.beginTransaction();
        try {
            res = db.insertOrThrow(T_BOOKINGSNACK, null, cv);

            if (res > 0) {
                String updateStockQuery = "UPDATE " + T_SNACK + " SET stock = stock - ? WHERE snack_id = ?";
                db.execSQL(updateStockQuery, new Object[]{quantity, snackId});

                double snackPrice = 0.0;
                Cursor priceCursor = db.rawQuery("SELECT price FROM " + T_SNACK + " WHERE snack_id = ?", new String[]{String.valueOf(snackId)});
                if (priceCursor.moveToFirst()) {
                    snackPrice = priceCursor.getDouble(0);
                }
                priceCursor.close();

                double cost = snackPrice * quantity;

                String updateBookingPriceQuery = "UPDATE " + T_BOOKING + " SET total_price = total_price + ? WHERE booking_id = ?";
                db.execSQL(updateBookingPriceQuery, new Object[]{cost, bookingId});

                db.setTransactionSuccessful();
            }
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "insertBookingSnack failed: " + e.getMessage());
            res = -1;
        } finally {
            db.endTransaction();
        }
        return res > 0;
    }

    public Cursor getBookingDetails(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                "B.booking_id, B.total_price, B.booking_date, B.status, " +
                "U.email AS user_email, " +
                "M.title AS movie_title, M.poster_url, " +
                "S.screen_number, S.date AS showtime_date, S.time AS showtime_time, S.ticket_price " +
                "FROM " + T_BOOKING + " B " +
                "INNER JOIN " + T_USERS + " U ON B.user_id = U.user_id " +
                "INNER JOIN " + T_SHOWTIMES + " S ON B.showtime_id = S.showtime_id " +
                "INNER JOIN " + T_MOVIES + " M ON S.movie_id = M.movie_id " +
                "WHERE B.booking_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(bookingId)});
    }

    public boolean payForBooking(int bookingId, int userId, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;

        try {
            ContentValues userValues = new ContentValues();
            userValues.put("balance", "balance - " + amount);

            Cursor c = db.rawQuery("SELECT balance FROM " + T_USERS + " WHERE user_id = ?", new String[]{String.valueOf(userId)});
            double currentBalance = 0.0;

            if (c.moveToFirst()) {
                currentBalance = c.getDouble(0);
            }
            c.close();

            if (currentBalance >= amount) {
                userValues.put("balance", currentBalance - amount);
                db.update(T_USERS, userValues, "user_id = ?", new String[]{String.valueOf(userId)});

                ContentValues bookingValues = new ContentValues();
                bookingValues.put("status", "Paid");
                bookingValues.put("booking_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

                int rowsUpdated = db.update(T_BOOKING, bookingValues, "booking_id = ?", new String[]{String.valueOf(bookingId)});

                if (rowsUpdated > 0) {
                    db.setTransactionSuccessful();
                    success = true;
                } else {
                    Log.e("DatabaseHelper", "Failed to update booking status for ID: " + bookingId);
                }
            } else {
                Log.e("DatabaseHelper", "User ID: " + userId + " has insufficient funds to pay " + amount);
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "payForBooking failed: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean cancelBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;

        try {
            Cursor bookingCursor = db.rawQuery(
                    "SELECT user_id, total_price, showtime_id FROM " + T_BOOKING +
                            " WHERE booking_id = ?",
                    new String[]{String.valueOf(bookingId)});

            int userId = -1;
            double totalPrice = 0.0;
            int showtimeId = -1;

            if (bookingCursor.moveToFirst()) {
                userId = bookingCursor.getInt(0);
                totalPrice = bookingCursor.getDouble(1);
                showtimeId = bookingCursor.getInt(2);
            }
            bookingCursor.close();

            if (userId == -1) return false;

            Cursor seatCursor = db.rawQuery(
                    "SELECT seat_id FROM " + T_BOOKINGSEAT +
                            " WHERE booking_id = ?",
                    new String[]{String.valueOf(bookingId)});

            List<Integer> seatIds = new ArrayList<>();
            while (seatCursor.moveToNext()) {
                seatIds.add(seatCursor.getInt(0));
            }
            seatCursor.close();

            for (int seatId : seatIds) {
                ContentValues seatValues = new ContentValues();
                seatValues.put("status", "Available");
                db.update(T_SEAT, seatValues, "seat_id = ?", new String[]{String.valueOf(seatId)});
            }

            Cursor snackCursor = db.rawQuery(
                    "SELECT snack_id, quantity FROM " + T_BOOKINGSNACK +
                            " WHERE booking_id = ?",
                    new String[]{String.valueOf(bookingId)});

            while (snackCursor.moveToNext()) {
                int snackId = snackCursor.getInt(0);
                int quantity = snackCursor.getInt(1);
                db.execSQL("UPDATE " + T_SNACK + " SET stock = stock + ? WHERE snack_id = ?",
                        new Object[]{quantity, snackId});
            }
            snackCursor.close();

            ContentValues bookingValues = new ContentValues();
            bookingValues.put("status", "Cancelled");
            db.update(T_BOOKING, bookingValues, "booking_id = ?",
                    new String[]{String.valueOf(bookingId)});

            db.delete(T_BOOKINGSEAT, "booking_id = ?",
                    new String[]{String.valueOf(bookingId)});
            db.delete(T_BOOKINGSNACK, "booking_id = ?",
                    new String[]{String.valueOf(bookingId)});

            db.execSQL("UPDATE " + T_USERS + " SET balance = balance + ? WHERE user_id = ?",
                    new Object[]{totalPrice, userId});

            db.setTransactionSuccessful();
            success = true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "cancelBooking failed: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public double getUserBalance(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        double balance = 0.0;

        try {
            String query = "SELECT balance FROM " + T_USERS + " WHERE user_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getUserBalance failed: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return balance;
    }

    public List<UserTickets.BookingSummary> getUserBookings(int userId) {
        List<UserTickets.BookingSummary> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        String query = "SELECT " +
                "B.booking_id, B.total_price, B.status, " +
                "M.title AS movie_title, " +
                "S.date AS showtime_date, S.time AS showtime_time " +
                "FROM " + T_BOOKING + " B " +
                "INNER JOIN " + T_SHOWTIMES + " S ON B.showtime_id = S.showtime_id " +
                "INNER JOIN " + T_MOVIES + " M ON S.movie_id = M.movie_id " +
                "WHERE B.user_id = ? " +
                "ORDER BY S.date DESC, S.time DESC";

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("booking_id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("movie_title"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("showtime_date"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("showtime_time"));
                    String dateTime = date + " @ " + time;

                    UserTickets.BookingSummary summary =
                            new UserTickets.BookingSummary(id, title, dateTime, price, status);
                    bookingList.add(summary);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getUserBookings failed: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return bookingList;
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + T_USERS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    public boolean updateEmail(int userId, String newEmail) {
        ContentValues cv = new ContentValues();
        cv.put("email", newEmail);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(T_USERS, cv, "user_id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updatePassword(int userId, String newPassword) {
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(T_USERS, cv, "user_id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateCreditCard(int userId, String newCard) {
        ContentValues cv = new ContentValues();
        cv.put("credit_card", newCard);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(T_USERS, cv, "user_id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public void updateBalance(int userId, double amountToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + T_USERS +
                        " SET balance = balance + ?" +
                        " WHERE user_id = ?",
                new Object[]{amountToAdd, userId});
    }

}