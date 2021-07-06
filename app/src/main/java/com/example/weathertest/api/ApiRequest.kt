package com.example.weathertest.api

import com.example.weathertest.model.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val API_ID = "e29366169c325d34b6d8f0995a15ebcd"

interface ApiRequest {

    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") location: String,
        @Query("appid") id: String = API_ID
    ): Call<WeatherData>

}