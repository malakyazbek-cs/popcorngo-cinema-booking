package com.example.popcorngo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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


public class UserMovies extends Fragment {

    private RecyclerView rvNowShowing, rvComingSoon, rvOldMovies;
    private NowShowingAdapter nowShowingAdapter;
    private MovieAdapter comingSoonAdapter;
    private MovieAdapter oldMoviesAdapter;
    private DatabaseHelper dbHelper;

    public UserMovies() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_movies, container, false);

        rvNowShowing = view.findViewById(R.id.rvNowShowing);
        rvComingSoon = view.findViewById(R.id.rvComingSoon);
        rvOldMovies = view.findViewById(R.id.rvOldMovies);

        rvNowShowing.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvComingSoon.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvOldMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        dbHelper = new DatabaseHelper(getContext());

        List<Movie> nowShowingList = loadMoviesByStatus("SHOWING_NOW");
        List<Movie> comingSoonList = loadMoviesByStatus("COMING_SOON");
        List<Movie> oldMoviesList = loadMoviesByStatus("OLD");

        nowShowingAdapter = new NowShowingAdapter(nowShowingList);
        comingSoonAdapter = new MovieAdapter(comingSoonList);
        oldMoviesAdapter = new MovieAdapter(oldMoviesList);

        rvNowShowing.setAdapter(nowShowingAdapter);
        rvComingSoon.setAdapter(comingSoonAdapter);
        rvOldMovies.setAdapter(oldMoviesAdapter);

        return view;
    }

    private List<Movie> loadMoviesByStatus(String status) {
        List<Movie> movies = new ArrayList<>();
        Cursor cursor = dbHelper.getMoviesCursorByStatus(status);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("movie_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"));
                String posterName = cursor.getString(cursor.getColumnIndexOrThrow("poster_url"));
                int posterResId = getResources().getIdentifier(posterName, "drawable", getContext().getPackageName());
                movies.add(new Movie(id, title, genre, duration, posterResId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public static class Movie {
        public int id;
        public String title;
        public String genre;
        public String duration;
        public int posterResId;

        public Movie(int id, String title, String genre, String duration, int posterResId) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.posterResId = posterResId;
        }
    }

    public static class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

        protected final List<Movie> movieList;

        public MovieAdapter(List<Movie> movieList) {
            this.movieList = movieList;
        }

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_movie, parent, false);
            return new MovieViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            Movie movie = movieList.get(position);
            holder.tvTitle.setText(movie.title);
            holder.tvGenre.setText(movie.genre);
            holder.tvDuration.setText(movie.duration);
            holder.ivPoster.setImageResource(movie.posterResId);
        }

        @Override
        public int getItemCount() {
            return movieList.size();
        }

        public static class MovieViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPoster;
            TextView tvTitle, tvGenre, tvDuration;

            public MovieViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPoster = itemView.findViewById(R.id.iv_movie_poster);
                tvTitle = itemView.findViewById(R.id.tv_movie_title);
                tvGenre = itemView.findViewById(R.id.tv_movie_genre);
                tvDuration = itemView.findViewById(R.id.tv_movie_duration);
            }
        }
    }

    public class NowShowingAdapter extends MovieAdapter {

        public NowShowingAdapter(List<Movie> movieList) {
            super(movieList);
        }

        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            Movie movie = movieList.get(position);
            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                int userID = -1;

                if (getActivity() instanceof UserDashboard) {
                    userID = ((UserDashboard) getActivity()).getUserId();
                }

                Intent intent = new Intent(context, MovieDetails.class);
                intent.putExtra("MOVIE_ID", movie.id);
                intent.putExtra("USER_ID", userID);
                context.startActivity(intent);
            });
        }
    }
}