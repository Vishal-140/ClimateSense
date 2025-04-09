package com.example.climatesense

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        private const val API_KEY = "f1ef9fd4b2f31e032bb0bb3fda4b5c16"
        private const val TAG = "Weather"
    }

    private lateinit var locationText: TextView
    private lateinit var btnChangeLocation: ImageButton
    private lateinit var temperatureValue: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var lastUpdated: TextView
    private lateinit var irrigationAdviceText: TextView
    private lateinit var plantingAdviceText: TextView
    private lateinit var alertMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect to TextViews
        locationText = findViewById(R.id.locationText)
        btnChangeLocation = findViewById(R.id.btnChangeLocation)
        temperatureValue = findViewById(R.id.temperatureValue)
        weatherDescription = findViewById(R.id.weatherDescription)
        lastUpdated = findViewById(R.id.lastUpdated)
        irrigationAdviceText = findViewById(R.id.irrigationAdviceText)
        plantingAdviceText = findViewById(R.id.plantingAdviceText)
        alertMessage = findViewById(R.id.alertMessage)

        // Set up location change button click listener
        btnChangeLocation.setOnClickListener {
            showLocationInputDialog()
        }

        // Call API for default location
        getWeatherData("Phagwara")

        // Fix for padding in notched screens
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showLocationInputDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Enter City Name")
            .setView(R.layout.dialog_location_input)
            .setPositiveButton("Search") { dialog, _ ->
                // This will be overridden below
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Get the EditText from the dialog and set up IME action
        val editText = dialog.findViewById<EditText>(R.id.locationEditText)
        editText?.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val city = textView.text.toString().trim()
                if (city.isNotEmpty()) {
                    getWeatherData(city)
                    dialog.dismiss()
                }
                true
            } else {
                false
            }
        }

        // Override the positive button to prevent dialog from closing if input is empty
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val city = editText?.text.toString().trim()
            if (city.isNotEmpty()) {
                getWeatherData(city)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeatherData(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)

        // Show loading state
        showLoading(true)

        api.getWeatherByCity(city, API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val weather = response.body()
                    updateUI(weather, city)

                    // Now get the 5-day forecast
                    getForecastData(city)
                } else {
                    Toast.makeText(this@MainActivity,
                        "City not found or API error: ${response.code()}",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@MainActivity,
                    "Network Error: ${t.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getForecastData(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)

        api.getForecastByCity(city, API_KEY).enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                if (response.isSuccessful) {
                    val forecast = response.body()
                    updateForecastUI(forecast)
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                // Silently fail for forecast - we already have current weather
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        // You could add a progress indicator here
        // For now, we'll just disable the location button while loading
        btnChangeLocation.isEnabled = !isLoading
    }

    private fun updateUI(weather: WeatherResponse?, city: String) {
        if (weather != null) {
            // Location
            locationText.text = weather.name ?: city

            // Temperature & Weather Description
            val tempInCelsius = weather.main.temp.minus(273.15).toInt()
            temperatureValue.text = "${tempInCelsius}°C"

            val description = weather.weather.firstOrNull()?.description ?: "N/A"
            weatherDescription.text = description.replaceFirstChar { it.uppercase() }

            // Last updated time
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = sdf.format(Date())
            lastUpdated.text = "Updated: $currentTime"

            // Generate farming recommendations based on weather
            generateFarmingRecommendations(weather)

            // Check for weather alerts
            checkWeatherAlerts(weather)
        }
    }

    private fun updateForecastUI(forecast: ForecastResponse?) {
        // Implementation for updating the 5-day forecast UI
        // This would need to update the day_forecast_item views
        // But would require access to those views which aren't in the provided XML
    }

    private fun generateFarmingRecommendations(weather: WeatherResponse) {
        val tempInCelsius = weather.main.temp.minus(273.15)
        val description = weather.weather.firstOrNull()?.description ?: ""

        // Simple irrigation advice based on temperature and weather
        val irrigationAdvice = when {
            description.contains("rain", ignoreCase = true) ->
                "No irrigation needed due to rainfall. Check soil moisture before next watering."
            tempInCelsius > 30 ->
                "Increase irrigation frequency. Water early morning or evening to reduce evaporation."
            tempInCelsius < 15 ->
                "Reduce irrigation. Soil retains moisture longer in cooler temperatures."
            else ->
                "Moderate irrigation recommended. Check soil moisture before watering."
        }

        // Simple planting advice based on temperature
        val plantingAdvice = when {
            tempInCelsius > 30 ->
                "Consider heat-resistant crops. Provide shade for sensitive plants."
            tempInCelsius < 15 ->
                "Good time for cold-weather crops. Protect sensitive plants from frost."
            tempInCelsius in 20.0..28.0 ->
                "Ideal conditions for most crops. Good time for planting."
            else ->
                "Standard planting conditions. Follow regular crop calendar."
        }

        irrigationAdviceText.text = irrigationAdvice
        plantingAdviceText.text = plantingAdvice
    }

    private fun checkWeatherAlerts(weather: WeatherResponse) {
        val tempInCelsius = weather.main.temp.minus(273.15)
        val description = weather.weather.firstOrNull()?.description ?: ""

        // Simple weather alerts based on conditions
        val alert = when {
            description.contains("thunderstorm", ignoreCase = true) ->
                "⚠️ Thunderstorm Alert: Take shelter and secure loose items."
            description.contains("rain", ignoreCase = true) && description.contains("heavy", ignoreCase = true) ->
                "⚠️ Heavy Rain Alert: Be aware of potential flooding."
            tempInCelsius > 35 ->
                "⚠️ Extreme Heat Alert: Take precautions against heat exposure."
            tempInCelsius < 5 ->
                "⚠️ Cold Weather Alert: Protect sensitive crops from frost."
            else ->
                "No current weather alerts"
        }

        alertMessage.text = alert
    }
}