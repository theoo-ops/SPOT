package com.example.spot.di

import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.fake.FakeFoodSpotRepository
import com.example.spot.location.FakeUserLocationProvider
import com.example.spot.location.UserLocationProvider

/** Hand-rolled dependency container. Swap the fakes for real implementations here. */
object AppGraph {
    val foodSpotRepository: FoodSpotRepository by lazy { FakeFoodSpotRepository() }
    val userLocationProvider: UserLocationProvider by lazy { FakeUserLocationProvider() }
}
