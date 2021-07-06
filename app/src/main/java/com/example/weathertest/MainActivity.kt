package com.example.weathertest

import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import com.example.weathertest.api.ApiRequest
import com.example.weathertest.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

const val BASE_URL = "https://api.openweathermap.org/"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

       getCurrentWeather("istanbul")

        getLocation()

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.search.clearFocus()
                getCurrentWeather(binding.search.query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }

    private fun getLocation() {
        val task = fusedLocationProviderClient.lastLocation
        val geocoder = Geocoder(this, Locale.getDefault())

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                val adress = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val city = adress[0].adminArea
                getCurrentWeather(city)
            }
        }
    }

    private fun getCurrentWeather(location: String) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiRequest::class.java)

        GlobalScope.launch(Dispatchers.Main) {
            val currentWeather = api.getWeather(location).await()

            val temp = String.format("%.0f", (currentWeather.main.temp - 273.15)).toDouble()
            val feelslike =
                String.format("%.0f", (currentWeather.main.feelsLike - 273.15)).toDouble()
            val humidity = currentWeather.main.humidity
            val visibility = currentWeather.visibility
            val wind = String.format("%.0f", currentWeather.wind.speed).toDouble()

            binding.apply {
                tvLocation.text = currentWeather.name
                tvStatus.text = currentWeather.weather[0].description
                tvTemperature.text = "$temp °C"
                tvFeelsLike.text = "Feels like $feelslike °C"
                tvHumidity.text = humidity.toString()
                tvVisibility.text = visibility.toString()
                tvWind.text = wind.toString()

                if (currentWeather.weather[0].description == "clear sky") {
                    ivWeather.setImageResource(R.drawable.ic_clearsky)
                } else if (currentWeather.weather[0].description == "few clouds") {
                    ivWeather.setImageResource(R.drawable.ic_fewclouds)
                } else if (currentWeather.weather[0].description == "scattered clouds") {
                    ivWeather.setImageResource(R.drawable.ic_scatterclouds)
                } else if (currentWeather.weather[0].description == "broken clouds") {
                    ivWeather.setImageResource(R.drawable.ic_brokenclouds)
                } else if (currentWeather.weather[0].description == "shower rain") {
                    ivWeather.setImageResource(R.drawable.ic_showerrain)
                } else if (currentWeather.weather[0].description == "rain") {
                    ivWeather.setImageResource(R.drawable.ic_rain)
                } else if (currentWeather.weather[0].description == "thunderstorm") {
                    ivWeather.setImageResource(R.drawable.ic_thunderstorm)
                } else if (currentWeather.weather[0].description == "snow") {
                    ivWeather.setImageResource(R.drawable.ic_snow)
                } else if (currentWeather.weather[0].description == "mist") {
                    ivWeather.setImageResource(R.drawable.ic_mist)
                }
            }
        }
    }
}