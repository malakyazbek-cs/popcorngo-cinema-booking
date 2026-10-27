package com.example.popcorngo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class UserSnacks extends Fragment {

    public interface UserIdProvider {
        int getUserId();
    }

    private RecyclerView recyclerSnacks;
    private UserSnacksAdapter adapter;
    private final List<Snack> snackList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private int currentUserId = -1;

    public UserSnacks() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof UserDashboard) {
            currentUserId = ((UserDashboard) getActivity()).getUserId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_snacks, container, false);

        recyclerSnacks = view.findViewById(R.id.recyclerSnacks);
        dbHelper = new DatabaseHelper(requireContext());
        recyclerSnacks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserSnacksAdapter(snackList, currentUserId);
        recyclerSnacks.setAdapter(adapter);

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

    private class UserSnacksAdapter extends RecyclerView.Adapter<UserSnacksAdapter.SnackViewHolder> {

        private final List<Snack> localSnackList;
        private final int userId;

        public UserSnacksAdapter(List<Snack> snackList, int userId) {
            this.localSnackList = snackList;
            this.userId = userId;
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
                Intent intent = new Intent(getContext(), BookingSelectionActivity.class);
                intent.putExtra("snack_id", snack.id);
                intent.putExtra("userId", userId);
                startActivity(intent);
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