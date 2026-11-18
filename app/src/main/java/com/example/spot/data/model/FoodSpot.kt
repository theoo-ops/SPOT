package com.example.spot.data.model

import androidx.annotation.DrawableRes

data class GeoPoint(val latitude: Double, val longitude: Double)

data class FoodSpot(
    val id: String,
    val name: String,
    val category: String,
    /** Average rating, or null when nobody has rated the spot yet. */
    val rating: Double?,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val hours: String,
    /** Local drawable for now; becomes an image URL once there is a backend. Null = no photo. */
    @param:DrawableRes val photo: Int? = null,
) {
    val location: GeoPoint get() = GeoPoint(latitude, longitude)
}
