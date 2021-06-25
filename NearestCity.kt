package com.example.myapplication.cities

data class NearestCity(
    val city: String,
    val country: String,
    val timeZone: String,
    val latitude: Double,
    val longitude: Double
)