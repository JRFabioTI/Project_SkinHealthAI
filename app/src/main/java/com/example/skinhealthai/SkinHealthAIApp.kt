package com.example.skinhealthai

import android.app.Application
import com.example.skinhealthai.data.network.RetrofitInstance // Importe o seu RetrofitInstance

class SkinHealthAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa o Context no RetrofitInstance assim que o aplicativo é criado.
        // Isso garante que o RetrofitInstance tenha um Context válido para acessar
        // as SharedPreferences e outras funcionalidades que dependam dele.
        RetrofitInstance.applicationContext = applicationContext
    }
}