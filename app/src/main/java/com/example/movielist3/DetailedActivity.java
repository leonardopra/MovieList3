package com.example.movielist3;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.movielist3.datas.MovieProvider;
import com.example.movielist3.datas.MovieTableHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.movielist3.model.Movie;
public class DetailedActivity extends AppCompatActivity implements IAddFavoriteDialogfragmentListener {

    ImageView mFavoriteImage;

    ImageView movieImagePoster;
    TextView movieOriginalTitle,
            movieReleaseDate,
            movieVoteAverage,
            moviePlot;

    int mStateFavorite;
    long id;
    int favoriteValue;
    FloatingActionButton fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        movieImagePoster = findViewById(R.id.iv_detail_movie_poster);
        movieOriginalTitle = findViewById(R.id.tv_original_title_movie);
        movieReleaseDate = findViewById(R.id.tv_release_date_movie);
        movieVoteAverage = findViewById(R.id.tv_vote_average_movie);
        moviePlot = findViewById(R.id.tv_plot_movie);
        fav = findViewById(R.id.floatingActionButtonFav);

        id = getIntent().getLongExtra("id_movie", 0);
        favoriteValue = getIntent().getIntExtra("favourite_value",0);

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.ID_MOVIE + "=" + id, null, null);
        Log.d("TAG", "onCreate: " + cursor);
        cursor.moveToNext();

        String imagePath = cursor.getString(cursor.getColumnIndex(MovieTableHelper.BACKDROP_PATH));
        String originalTitle = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
        double voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
        String releaseDate = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
        String plot = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));

        setTitle(originalTitle);
        mStateFavorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));

        switch (mStateFavorite) {
            case 0:
                fav.setImageResource(R.drawable.ic_favorite_border_white_24px);
                break;

            case 1:
                fav.setImageResource(R.drawable.ic_favorite_white_24px);
                break;
        }


        movieOriginalTitle.setText(originalTitle);
        movieReleaseDate.setText(releaseDate);
        movieVoteAverage.setText("" + voteAverage);
        moviePlot.setText(plot);
        Glide.with(this)
                .load(Constants.MOVIE_POSTER_W500_PATH + imagePath)
                .into(movieImagePoster);

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();

                if (favoriteValue == 0) {
                    AddFavoriteDialogfragment dialogfragment = new AddFavoriteDialogfragment("Aggiungere ai preferiti?", "", id);
                    dialogfragment.show(fm, "dialogFragmentFavorite");

                } else if (favoriteValue == 1) {
                    AddFavoriteDialogfragment dialogfragment = new AddFavoriteDialogfragment("Rimuovere dai preferiti?", "", id);
                    dialogfragment.show(fm, "dialogFragmentFavorite");

                }
            }
        });

    }


    @Override
    public void onPositiveClick(long id) {
        Cursor cursor = getApplicationContext().getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.ID_MOVIE + "=" + id, null, null);
        cursor.moveToNext();
        int favorite = 0;

        favorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));
        ContentValues values = new ContentValues();
        if (favorite == 0) {
            values.put(MovieTableHelper.FAVORITE, 1);
            fav.setImageResource(R.drawable.ic_favorite_white_24px);
            Intent Back = new Intent(DetailedActivity.this , MainActivity.class);
            Back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(Back);
            Toast.makeText(getApplicationContext(), "Film Aggiunto", Toast.LENGTH_LONG).show();

        } else if (favorite == 1) {
            values.put(MovieTableHelper.FAVORITE, 0);
            fav.setImageResource(R.drawable.ic_favorite_border_white_24px);
            Intent Back = new Intent(DetailedActivity.this , MainActivity.class);
            Back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(Back);
            Toast.makeText(getApplicationContext(), "Film Rimosso", Toast.LENGTH_LONG).show();

        }

        getContentResolver().update(MovieProvider.MOVIES_URI, values, MovieTableHelper.ID_MOVIE + " = " + id, null);
        cursor.close();
    }

    @Override
    public void onNegativeClick() {}

    public void onBackPressed(){
        Intent Back = new Intent(DetailedActivity.this , MainActivity.class);
        Back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Back);
    }
}
