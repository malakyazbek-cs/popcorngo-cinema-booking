package com.example.popcorngo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SnacksFragment extends Fragment {

    private RecyclerView recyclerSnacks;
    private FloatingActionButton btnAddSnack;
    private SnacksAdapter adapter;
    private final List<Snack> snackList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    public SnacksFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_snacks, container, false);
        recyclerSnacks = view.findViewById(R.id.recyclerSnacks);
        btnAddSnack = view.findViewById(R.id.btnAddSnack);
        dbHelper = new DatabaseHelper(requireContext());
        recyclerSnacks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SnacksAdapter(snackList);
        recyclerSnacks.setAdapter(adapter);
        btnAddSnack.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEditSnackActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSnacks();
    }

    private void loadSnacks() {
        if (getContext() == null) return;
        snackList.clear();
        snackList.addAll(dbHelper.getAllSnacks());
        adapter.notifyDataSetChanged();
    }

    private void deleteSnackAndRefresh(int snackId, String snackName) {
        boolean deleted = dbHelper.deleteSnack(snackId);
        if (deleted) {
            Toast.makeText(requireContext(), snackName + " deleted successfully.", Toast.LENGTH_SHORT).show();
            loadSnacks();
        } else {
            Toast.makeText(requireContext(), "Failed to delete " + snackName + ".", Toast.LENGTH_SHORT).show();
        }
    }

    private class SnacksAdapter extends RecyclerView.Adapter<SnacksAdapter.SnackViewHolder> {

        private final List<Snack> localSnackList;

        public SnacksAdapter(List<Snack> snackList) {
            this.localSnackList = snackList;
        }

        @NonNull
        @Override
        public SnackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.snack_item, parent, false);
            return new SnackViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SnackViewHolder holder, int position) {
            Snack snack = localSnackList.get(position);

            holder.tvName.setText(snack.name);
            holder.tvPrice.setText(String.format(Locale.US, "$%.2f", snack.price));
            holder.tvStock.setText("Stock : " + snack.stock);

            if (snack.imageUrl != null && (snack.imageUrl.startsWith("content://") || snack.imageUrl.startsWith("file://"))) {
                try {
                    holder.ivSnackImage.setImageURI(Uri.parse(snack.imageUrl));
                } catch (Exception e) {
                    holder.ivSnackImage.setImageResource(R.drawable.imgplaceholder);
                }
            } else {
                int imageResourceId = holder.itemView.getContext().getResources().getIdentifier(
                        snack.imageUrl,
                        "drawable",
                        holder.itemView.getContext().getPackageName()
                );

                if (imageResourceId != 0) {
                    holder.ivSnackImage.setImageResource(imageResourceId);
                } else {
                    holder.ivSnackImage.setImageResource(R.drawable.imgplaceholder);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddEditSnackActivity.class);
                intent.putExtra("snack_id", snack.id);
                startActivity(intent);
            });

            holder.itemView.setOnLongClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Snack")
                        .setMessage("Are you sure you want to delete this snack '" + snack.name + "'? This action cannot be undone.")
                        .setPositiveButton("Delete", (d, w) ->
                                deleteSnackAndRefresh(snack.id, snack.name))
                        .setNegativeButton("Cancel", null)
                        .show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));

                return true;
            });
        }

        @Override
        public int getItemCount() {
            return localSnackList.size();
        }

        class SnackViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvStock;
            ImageView ivSnackImage;

            public SnackViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.snackName);
                tvPrice = itemView.findViewById(R.id.snackPrice);
                tvStock = itemView.findViewById(R.id.snackStock);
                ivSnackImage = itemView.findViewById(R.id.snackImage);
            }
        }
    }
}