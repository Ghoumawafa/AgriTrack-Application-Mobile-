package com.example.agritrack.Api;

import com.example.agritrack.Models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String cityName,
            @Query("units") String units,
            @Query("lang") String language,
            @Query("appid") String apiKey
    );

    @GET("weather")
    Call<WeatherResponse> getWeatherByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("lang") String language,
            @Query("appid") String apiKey
    );
}