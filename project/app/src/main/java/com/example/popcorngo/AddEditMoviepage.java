package com.example.popcorngo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;


public class AddEditMoviepage extends AppCompatActivity {

    private TextInputEditText etTitle, etSynopsis, etDuration, etGenre, etTrailerUrl;
    private ImageView ivPoster;
    private Button btnUploadPoster, btnCancel, btnSave;
    private DatabaseHelper dbHelper;
    private int movieIdToEdit = -1;
    private String currentPosterUrl = "default_poster";
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_moviepage);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            ivPoster.setImageURI(uri);
                            currentPosterUrl = uri.toString();
                            Toast.makeText(this, "Image selected successfully.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        initializeViews();
        dbHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("MOVIE_ID")) {
            movieIdToEdit = getIntent().getIntExtra("MOVIE_ID", -1);
            loadMovieDetailsForEditing(movieIdToEdit);
            setTitle("Edit Movie");
        } else {
            ivPoster.setImageResource(R.drawable.imgplaceholder);
            setTitle("Add New Movie");
        }

        btnCancel.setOnClickListener(v -> closeActivity());
        btnSave.setOnClickListener(v -> saveOrUpdateMovie());
        btnUploadPoster.setOnClickListener(v -> handlePosterUpload());
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_title);
        etSynopsis = findViewById(R.id.et_synopsis);
        etDuration = findViewById(R.id.et_duration);
        etGenre = findViewById(R.id.et_genre);
        etTrailerUrl = findViewById(R.id.et_trailer_url);
        ivPoster = findViewById(R.id.iv_poster);
        btnUploadPoster = findViewById(R.id.btn_upload_poster);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void loadMovieDetailsForEditing(int movieId) {
        String[] movieData = dbHelper.getMovieById(movieId);

        if (movieData != null) {
            etTitle.setText(movieData[1]);
            etSynopsis.setText(movieData[2]);
            etDuration.setText(movieData[3]);
            etGenre.setText(movieData[4]);
            currentPosterUrl = movieData[5];
            etTrailerUrl.setText(movieData[6]);

            if (currentPosterUrl != null && !currentPosterUrl.isEmpty()) {
                if (currentPosterUrl.startsWith("content:") || currentPosterUrl.startsWith("file:")) {
                    try {
                        Uri imageUri = Uri.parse(currentPosterUrl);
                        ivPoster.setImageURI(imageUri);
                    } catch (Exception e) {
                        ivPoster.setImageResource(R.drawable.imgplaceholder);
                    }
                } else {
                    int resourceId = getResources().getIdentifier(currentPosterUrl, "drawable", getPackageName());
                    if (resourceId != 0) {
                        ivPoster.setImageResource(resourceId);
                    } else {
                        ivPoster.setImageResource(R.drawable.imgplaceholder);
                    }
                }
            } else {
                ivPoster.setImageResource(R.drawable.imgplaceholder);
            }
        } else {
            Toast.makeText(this, "Error : Movie not found.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveOrUpdateMovie() {
        String title = etTitle.getText().toString().trim();
        String synopsis = etSynopsis.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String trailerUrl = etTrailerUrl.getText().toString().trim();
        String posterUrl = currentPosterUrl;
        String status = "COMING_SOON";
        int duration;

        if (title.isEmpty() || durationStr.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Title, Genre and Duration are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Duration must be a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (movieIdToEdit != -1) {
            int result = dbHelper.updateMovie(movieIdToEdit, title, synopsis, duration, genre, posterUrl, trailerUrl, status);
            if (result > 0) {
                Toast.makeText(this, title + " updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            long id = dbHelper.addMovie(title, synopsis, duration, genre, posterUrl, trailerUrl, status);
            if (id > 0) {
                Toast.makeText(this, title + " added successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Add failed.", Toast.LENGTH_SHORT).show();
            }
        }

        closeActivity();
    }

    private void handlePosterUpload() {
        imagePickerLauncher.launch("image/*");
    }

    private void closeActivity() {
        finish();
    }
}