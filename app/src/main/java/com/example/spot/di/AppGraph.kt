package com.example.spot.di

import android.content.Context
import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.fake.DebugData
import com.example.spot.data.fake.FakeFoodSpotRepository
import com.example.spot.data.fake.SeedFoodSpots
import com.example.spot.location.FusedUserLocationProvider
import com.example.spot.location.UserLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Hand-rolled dependency container, initialised once in SpotApplication. */
object AppGraph {
    private lateinit var appContext: Context

    /** Lives as long as the process; shared flows (like location) run in it. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val foodSpotRepository: FoodSpotRepository by lazy {
        FakeFoodSpotRepository(spots = DebugData.spotsOr(SeedFoodSpots)) // DebugData.Enabled = worst-case data
    }
    val userLocationProvider: UserLocationProvider by lazy { FusedUserLocationProvider(appContext, appScope) }
}
