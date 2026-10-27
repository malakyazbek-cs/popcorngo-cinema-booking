package com.example.popcorngo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


public class ReportActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ImageButton btnBack;
    private RecyclerView bookingsRecycler, revenueRecycler, snacksRecycler;
    private SimpleReportAdapter bookingsAdapter, revenueAdapter, snacksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = new DatabaseHelper(this);

        bookingsRecycler = findViewById(R.id.recyclerBookings);
        revenueRecycler = findViewById(R.id.recyclerRevenue);
        snacksRecycler = findViewById(R.id.recyclerSnacks);
        btnBack = findViewById(R.id.btn_back);

        bookingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        revenueRecycler.setLayoutManager(new LinearLayoutManager(this));
        snacksRecycler.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ReportActivity.this, AdminDashboard.class);
            startActivity(intent);
            finish();
        });

        loadBookingsReport();
        loadRevenueReport();
        loadSnackReport();
    }

    private void loadBookingsReport() {
        Cursor cursor = dbHelper.getBookingsByMovie(true);
        List<ReportItem> items = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idxTitle = cursor.getColumnIndex("title");
                int idxTotal = cursor.getColumnIndex("total_bookings");
                String title = idxTitle != -1 ? cursor.getString(idxTitle) : "N/A";
                String total = idxTotal != -1 ? String.valueOf(cursor.getInt(idxTotal)) : "0";
                items.add(new ReportItem(title, total));
            } while (cursor.moveToNext());
            cursor.close();
        }
        bookingsAdapter = new SimpleReportAdapter(items);
        bookingsRecycler.setAdapter(bookingsAdapter);
    }

    private void loadRevenueReport() {
        Cursor cursor = dbHelper.getRevenueOverTime("month");
        List<ReportItem> items = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idxPeriod = cursor.getColumnIndex("period");
                int idxRevenue = cursor.getColumnIndex("revenue");
                String periodRaw = idxPeriod != -1 ? cursor.getString(idxPeriod) : "N/A";
                double revenueVal = idxRevenue != -1 ? cursor.getDouble(idxRevenue) : 0;
                String period = "N/A";
                try {
                    int monthNum = Integer.parseInt(periodRaw);
                    String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
                    if(monthNum >=1 && monthNum <= 12){
                        period = months[monthNum-1] + " 2025";
                    }
                } catch (NumberFormatException e) {
                    period = "Period " + periodRaw;
                }
                String revenue = "$" + String.format("%.2f", revenueVal);
                items.add(new ReportItem(period, revenue));
            } while (cursor.moveToNext());
            cursor.close();
        }
        revenueAdapter = new SimpleReportAdapter(items);
        revenueRecycler.setAdapter(revenueAdapter);
    }

    private void loadSnackReport() {
        Cursor cursor = dbHelper.getTopSellingSnacks();
        List<ReportItem> items = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idxName = cursor.getColumnIndex("name");
                int idxSold = cursor.getColumnIndex("total_sold");
                String name = idxName != -1 ? cursor.getString(idxName) : "N/A";
                String sold = idxSold != -1 ? String.valueOf(cursor.getInt(idxSold)) : "0";
                items.add(new ReportItem(name, sold));
            } while (cursor.moveToNext());
            cursor.close();
        }
        snacksAdapter = new SimpleReportAdapter(items);
        snacksRecycler.setAdapter(snacksAdapter);
    }

    public static class ReportItem {
        public String label;
        public String value;
        public ReportItem(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class SimpleReportAdapter extends RecyclerView.Adapter<SimpleReportAdapter.ViewHolder> {

        private List<ReportItem> items;
        public SimpleReportAdapter(List<ReportItem> items) {
            this.items = items;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView label, value;
            public ViewHolder(View view) {
                super(view);
                label = view.findViewById(R.id.labelText);
                value = view.findViewById(R.id.valueText);
            }
        }

        @Override
        public SimpleReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleReportAdapter.ViewHolder holder, int position) {
            ReportItem item = items.get(position);
            holder.label.setText(item.label);
            holder.value.setText(item.value);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}
