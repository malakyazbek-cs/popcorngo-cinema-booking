package com.example.popcorngo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;


public class DashboardFragment extends Fragment {

    private TextView tvTotalMovies, tvBookingsToday, tvBookingsWeek;
    private TextView tvSeatsOccupancy, tvLowStockSnacks;
    private DatabaseHelper db;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        tvTotalMovies = view.findViewById(R.id.tvTotalMovies);
        tvBookingsToday = view.findViewById(R.id.tvBookingsToday);
        tvBookingsWeek = view.findViewById(R.id.tvBookingsWeek);
        tvSeatsOccupancy = view.findViewById(R.id.tvSeatsOccupancy);
        tvLowStockSnacks = view.findViewById(R.id.tvLowStockSnacks);

        loadDashboardData();
    }

    private void loadDashboardData() {
        int totalMovies = db.getTotalMovies();

        int bookingsToday = db.getBookingsToday();

        int bookingsWeek = db.getBookingsThisWeek();

        double seatsOccupancy = db.getSeatsOccupancy();

        List<String> lowStockSnacksList = db.getLowStockSnacks(5);

        String lowStockSnacks = lowStockSnacksList.isEmpty()
                ? "No low-stock items"
                : TextUtils.join("\n", lowStockSnacksList);

        tvTotalMovies.setText(String.valueOf(totalMovies));

        tvBookingsToday.setText(String.valueOf(bookingsToday));

        tvBookingsWeek.setText(String.valueOf(bookingsWeek));

        String seats = seatsOccupancy + "%";
        tvSeatsOccupancy.setText(seats);

        tvLowStockSnacks.setText(lowStockSnacks);

    }
}
