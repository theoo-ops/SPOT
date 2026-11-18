package com.example.spot

import android.app.Application
import com.example.spot.di.AppGraph

class SpotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
