package com.example.climatesense

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)

data class City(
    val id: Int,
    val name: String,
    val country: String
)