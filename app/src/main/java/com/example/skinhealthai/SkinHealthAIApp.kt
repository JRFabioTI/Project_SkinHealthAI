package com.example.skinhealthai

import android.app.Application
import com.example.skinhealthai.data.network.RetrofitInstance

class SkinHealthAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.applicationContext = applicationContext
    }
}