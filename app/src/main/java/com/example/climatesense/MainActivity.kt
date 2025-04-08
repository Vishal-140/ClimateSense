package com.example.climatesense

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Initialize with demo data
        updateWeatherData()
    }

    private fun updateWeatherData() {
        // Here you would normally fetch data from an API
        // For now, just display sample data

        // You can access your views and set their values
        // Example:
        // findViewById<TextView>(R.id.temperatureValue).text = "28°C"
        // findViewById<TextView>(R.id.humidityText).text = "72%"
        // etc.
    }
}