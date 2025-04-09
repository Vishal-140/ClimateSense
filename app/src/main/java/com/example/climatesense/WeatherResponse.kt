package com.example.climatesense

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val name: String,
    val wind: Wind = Wind(0.0, 0),
    val sys: Sys = Sys("", 0, 0),
    val dt: Long = 0
)

data class Weather(
    val id: Int = 0,
    val main: String = "",
    val description: String = "",
    val icon: String = ""
)

data class Main(
    val temp: Double = 0.0,
    val feels_like: Double = 0.0,
    val temp_min: Double = 0.0,
    val temp_max: Double = 0.0,
    val pressure: Int = 0,
    val humidity: Int = 0
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)