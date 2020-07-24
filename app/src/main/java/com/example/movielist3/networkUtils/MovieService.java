package com.example.movielist3.networkUtils;

import com.example.movielist3.model.MovieCollection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieService {

    @GET("movie/popular")
    Call<MovieCollection> getPopularMovies(@Query("api_key") String apiKey,
                                           @Query("language") String language,
                                           @Query("page") int page);


    @GET("movie/top_rated")
    Call<MovieCollection> getTopRatedMovies(@Query("api_key") String apiKey,
                                            @Query("language") String language,
                                            @Query("page") int page);


}
