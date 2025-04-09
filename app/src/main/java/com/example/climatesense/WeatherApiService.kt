package com.example.climatesense

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>

    @GET("forecast")
    fun getForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<ForecastResponse>
}