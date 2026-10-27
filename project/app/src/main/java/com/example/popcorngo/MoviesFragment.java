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


public class MoviesFragment extends Fragment {

    private RecyclerView rvMovies;
    private FloatingActionButton btnAddMovie;
    private MovieAdapter movieAdapter;
    private final List<Movie> movieList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    private static class Movie {
        int movieId, duration;
        String title, synopsis;
        String genre, posterUrl, trailerUrl, status;

        public Movie(int movieId, String title, String genre, int duration, String posterUrl) {
            this.movieId = movieId;
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.posterUrl = posterUrl;
        }

        public Movie(int movieId, String title, String synopsis, int duration, String genre, String posterUrl, String trailerUrl, String status) {
            this.movieId = movieId;
            this.title = title;
            this.synopsis = synopsis;
            this.duration = duration;
            this.genre = genre;
            this.posterUrl = posterUrl;
            this.trailerUrl = trailerUrl;
            this.status = status;
        }
    }

    public MoviesFragment() {
        super(R.layout.fragment_movies);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        rvMovies = view.findViewById(R.id.rvMovies);
        btnAddMovie = view.findViewById(R.id.btnAddMovie);

        rvMovies.setLayoutManager(new LinearLayoutManager(getContext()));
        movieAdapter = new MovieAdapter(movieList);
        rvMovies.setAdapter(movieAdapter);

        loadMovies();

        btnAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditMoviepage.class);
            intent.putExtra("MODE", "ADD");
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMovies();
    }

    private void loadMovies() {
        if (getContext() == null) return;

        dbHelper = new DatabaseHelper(requireActivity());
        List<String[]> rawMovies = dbHelper.getAllMovies();

        movieList.clear();

        for (String[] rawData : rawMovies) {
            try {
                int id = Integer.parseInt(rawData[0]);
                String title = rawData[1];
                String genre = rawData[4];
                int duration = Integer.parseInt(rawData[3]);
                String posterUrl = rawData[5];

                movieList.add(new Movie(id, title, genre, duration, posterUrl));
            } catch (NumberFormatException e) {
            }
        }

        movieAdapter.notifyDataSetChanged();
    }

    private void deleteAndRefresh(int movieId, String movieTitle) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(requireActivity());
        }

        int rowsDeleted = dbHelper.deleteMovie(movieId);

        if (rowsDeleted > 0) {
            Toast.makeText(requireContext(), movieTitle + " deleted successfully.", Toast.LENGTH_SHORT).show();
            loadMovies();
        } else {
            Toast.makeText(requireContext(), "Failed to delete " + movieTitle + ".", Toast.LENGTH_SHORT).show();
        }
    }


    private class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

        private final List<Movie> localMovieList;

        public MovieAdapter(List<Movie> movieList) {
            this.localMovieList = movieList;
        }

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie, parent, false);
            return new MovieViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            Movie movie = localMovieList.get(position);

            holder.tvTitle.setText(movie.title);
            holder.tvGenre.setText(movie.genre);
            holder.tvDuration.setText(movie.duration + " min");

            if (movie.posterUrl != null && (movie.posterUrl.startsWith("content://") || movie.posterUrl.startsWith("file://"))) {
                try {
                    holder.ivPoster.setImageURI(Uri.parse(movie.posterUrl));
                } catch (Exception e) {
                    holder.ivPoster.setImageResource(R.drawable.imgplaceholder);
                }
            } else {
                int imageResourceId = holder.itemView.getContext().getResources().getIdentifier(
                        movie.posterUrl,
                        "drawable",
                        holder.itemView.getContext().getPackageName()
                );

                if (imageResourceId != 0) {
                    holder.ivPoster.setImageResource(imageResourceId);
                } else {
                    holder.ivPoster.setImageResource(R.drawable.imgplaceholder);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), AddEditMoviepage.class);
                intent.putExtra("MODE", "EDIT");
                intent.putExtra("MOVIE_ID", movie.movieId);
                holder.itemView.getContext().startActivity(intent);
            });

            holder.itemView.setOnLongClickListener(v -> {

                AlertDialog dialog = new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete Movie")
                        .setMessage("Are you sure you want to delete this movie '" + movie.title + "' ? This action cannot be undone.")
                        .setPositiveButton("Delete", (d, w) ->
                                MoviesFragment.this.deleteAndRefresh(movie.movieId, movie.title))
                        .setNegativeButton("Cancel", null)
                        .show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));

                return true;
            });
        }

        @Override
        public int getItemCount() {
            return localMovieList.size();
        }

        public class MovieViewHolder extends RecyclerView.ViewHolder {
            final TextView tvTitle;
            final TextView tvGenre;
            final TextView tvDuration;
            final ImageView ivPoster;

            public MovieViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_movie_title);
                tvGenre = itemView.findViewById(R.id.tv_movie_genre);
                tvDuration = itemView.findViewById(R.id.tv_movie_duration);
                ivPoster = itemView.findViewById(R.id.iv_movie_poster);
            }
        }
    }
}