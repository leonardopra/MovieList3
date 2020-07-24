package com.example.movielist3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movielist3.adapter.MovieAdapter;
import com.example.movielist3.datas.MovieProvider;
import com.example.movielist3.datas.MovieTableHelper;
import com.example.movielist3.model.Movie;
import com.example.movielist3.model.MovieCollection;
import com.example.movielist3.networkUtils.MovieService;
import com.example.movielist3.networkUtils.WebService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler{

    private WebService webService;

    private MovieAdapter movieAdapter;
    RecyclerView movieRecycleView;

    TextView mErrorMessage;
    Button mCaricaButton;


    private boolean loading = true;
    List<Movie> mMovieList;
    int initId = 0;
    int lastPosition;
    int pastVisiblesItems,visibleItemCount,totalItemCount,mStateList = 0,mOldStateList = 0;
    private int currentPage = 1;

    GridLayoutManager layoutManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mMovieList = new ArrayList<>();

        webService = WebService.getInstance();

        movieRecycleView = findViewById(R.id.recyclerview_movie);


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 2);
        }

        mErrorMessage = findViewById(R.id.error_message);
        mCaricaButton = findViewById(R.id.btn_carica);
        movieRecycleView.setLayoutManager(layoutManager);
        movieRecycleView.setHasFixedSize(true);
        movieAdapter = new MovieAdapter(this,this);
        movieRecycleView.setAdapter(movieAdapter);


        if (isDbEmpty()) {
            if (isOnline()) {
                mErrorMessage.setVisibility(View.INVISIBLE);
                mCaricaButton.setVisibility(View.VISIBLE);
                movieRecycleView.setVisibility(View.VISIBLE);
                loadNextPageMovies(currentPage);
            } else {
                mErrorMessage.setText("Non ho trovato film. Attiva internet");
                Toast.makeText(this, "Verifica la connessione", Toast.LENGTH_LONG).show();
            }
        } else {
            movieAdapter.setMoviesData(loadMoviesFromDb());
            movieAdapter.notifyDataSetChanged();
            if (savedInstanceState != null) {

                    if (mStateList == 0) {
                        mMovieList = loadMoviesFromDb();
                        movieAdapter.setMoviesData(mMovieList);
                        movieAdapter.notifyDataSetChanged();
                    } else if (mStateList == 1) {
                        mMovieList = loadMoviesFromDb();
                        movieAdapter.setMoviesData(mMovieList);
                        movieAdapter.notifyDataSetChanged();
                    } else if (mStateList == 2) {
                        mMovieList = loadMoviesFromDb();
                        movieAdapter.setMoviesData(mMovieList);
                        movieAdapter.notifyDataSetChanged();
                    }

            } else {
                mCaricaButton.setVisibility(View.INVISIBLE);
                mMovieList = loadMoviesFromDb();
                movieAdapter.setMoviesData(mMovieList);
                movieAdapter.notifyDataSetChanged();
            }
        }


        if (savedInstanceState != null) {
            lastPosition = savedInstanceState.getInt("h");
        }


        movieRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();


                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (mStateList == 0)) {
                        if (initId == 0) initId = 20;
                        if (getCountMoviesDb() > initId) {
                            List<Movie> newMovieList = loadMoviesFromDb();
                            if (newMovieList.size() == 20) {
                                mMovieList.addAll(newMovieList);
                                movieAdapter.setMoviesData(mMovieList);
                                movieAdapter.notifyDataSetChanged();
                                initId += 20;
                            } else {
                                loadNextPageMovies(currentPage);
                                currentPage++;
                            }
                        } else {
                            loadNextPageMovies(currentPage);
                            currentPage++;
                        }

                    } else if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (mStateList == 1)) {
                        if (initId == 0) initId = 20;
                        if (getCountMoviesDb() > initId) {
                            List<Movie> newMovieList = loadMoviesFromDb();
                            if (newMovieList.size() == 20) {
                                mMovieList.addAll(newMovieList);
                                movieAdapter.setMoviesData(mMovieList);
                                movieAdapter.notifyDataSetChanged();
                                initId += 20;
                            } else {
                                loadNextPageMovies(currentPage);
                                currentPage++;
                            }
                        } else {
                            loadNextPageMovies(currentPage);
                            currentPage++;
                        }
                    }
                }
            }
        });


        mCaricaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mStateList == 0) {
                    mErrorMessage.setVisibility(View.INVISIBLE);
                    mCaricaButton.setVisibility(View.INVISIBLE);
                    movieRecycleView.setVisibility(View.VISIBLE);
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);

                } else if (mStateList == 1) {
                    mErrorMessage.setVisibility(View.INVISIBLE);
                    mCaricaButton.setVisibility(View.INVISIBLE);
                    movieRecycleView.setVisibility(View.VISIBLE);
                    mMovieList = loadMoviesFromDb();
                    movieAdapter.setMoviesData(mMovieList);
                }
            }
        });


    }


    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mMovieList);
        outState.putInt("h", pastVisiblesItems);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        movieAdapter.setMoviesData((List<Movie>) savedInstanceState.get("list"));
        movieRecycleView.scrollToPosition(lastPosition);

    }

    @Override
    public void onClick(Movie clickedMovie) {

        Intent StartDetailActivity = new Intent(MainActivity.this, DetailedActivity.class);

        StartDetailActivity.putExtra("id_movie", clickedMovie.getId());
        StartDetailActivity.putExtra("favourite_value", clickedMovie.getFavorite());

        startActivity(StartDetailActivity);
    }

    @Override
    public void onLongClick(Movie clickedMovie) {


    }

    private int getCountMoviesDb() {

        Cursor cursor = null;

        if (mStateList == 0 || mStateList == 1)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, null, null);
        else if (mStateList == 2)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.FAVORITE + " = " + 1, null, null, null);

        return cursor.getCount();
    }

    public void loadNextPageMovies(int page) {

        if (isOnline()) {

            MovieService movieService = WebService.movieService(webService);

            Call<MovieCollection> collectionCall = null;

            if (mStateList == 0)
                collectionCall = movieService.getPopularMovies(Constants.API_KEY, "it-IT", page);
            else if (mStateList == 1)
                collectionCall = movieService.getTopRatedMovies(Constants.API_KEY, "it-IT", page);

            collectionCall.enqueue(new Callback<MovieCollection>() {
                @Override
                public void onResponse(Call<MovieCollection> call, Response<MovieCollection> response) {
                    List<Movie> movieList = response.body().getMovieResults();


                    for (Movie movie : movieList) {

                        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.ID_MOVIE + " = " + movie.getId(), null, null);

                        if (cursor != null && !cursor.moveToNext()) {

                            ContentValues values = new ContentValues();
                            values.put(MovieTableHelper.ID_MOVIE, movie.getId());
                            values.put(MovieTableHelper.TITLE, movie.getOriginalTitle());
                            values.put(MovieTableHelper.DESCRIPTION, movie.getOverview());
                            values.put(MovieTableHelper.POSTER_PATH, movie.getPosterPath());
                            values.put(MovieTableHelper.RELEASE_DATE, movie.getReleaseDate());
                            values.put(MovieTableHelper.VOTE, movie.getVoteAverage());
                            values.put(MovieTableHelper.POPULARITY, movie.getPopularity());
                            values.put(MovieTableHelper.VOTE_COUNT, movie.getVoteCount());
                            values.put(MovieTableHelper.BACKDROP_PATH, movie.getBackdropPath());

                            cursor.close();
                            getContentResolver().insert(MovieProvider.MOVIES_URI, values);

                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieCollection> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Verifica la connessione", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public List<Movie> loadMoviesFromDb() {

        Cursor cursor = null;

        if (mStateList == 0)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, MovieTableHelper.POPULARITY + " DESC LIMIT " + initId + ",20", null);
        else if (mStateList == 1)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, MovieTableHelper.VOTE_COUNT + " DESC LIMIT " + initId + ",20", null);
        else if (mStateList == 2)
            cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, MovieTableHelper.FAVORITE + " = " + 1, null, null, null);


        List<Movie> list = new ArrayList<>();

        if (cursor != null && cursor.getCount() != 0) {

            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {

                int id = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.ID_MOVIE));
                String title = cursor.getString(cursor.getColumnIndex(MovieTableHelper.TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(MovieTableHelper.DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndex(MovieTableHelper.RELEASE_DATE));
                Double vote = cursor.getDouble(cursor.getColumnIndex(MovieTableHelper.VOTE));
                String poster = cursor.getString(cursor.getColumnIndex(MovieTableHelper.POSTER_PATH));
                int favorite = cursor.getInt(cursor.getColumnIndex(MovieTableHelper.FAVORITE));
                float popularity = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.POPULARITY));
                float voteCount = cursor.getFloat(cursor.getColumnIndex(MovieTableHelper.VOTE_COUNT));
                String backdropPath = cursor.getString(cursor.getColumnIndex(MovieTableHelper.BACKDROP_PATH));

                list.add(new Movie(id, title, poster, desc, vote, date, favorite, popularity, voteCount, backdropPath));
                cursor.moveToNext();
            }
        }

        cursor.close();
        return list;

    }

    public boolean isDbEmpty() {

        Cursor cursor = getContentResolver().query(MovieProvider.MOVIES_URI, null, null, null, null, null);

        if (cursor != null && !cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();

            return false;
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorite) {
            mStateList = 2;
            mOldStateList = 2;
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
            return true;
        }
        if (id == R.id.action_popular) {
            mStateList = 0;
            mOldStateList = 0;
            initId = 0;
            currentPage = 1;
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
            return true;
        }

        if (id == R.id.action_top_rated) {
            mStateList = 1;
            mOldStateList = 1;
            initId = 0;
            currentPage = 1;
            mMovieList = loadMoviesFromDb();
            movieAdapter.setMoviesData(mMovieList);
            movieAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}