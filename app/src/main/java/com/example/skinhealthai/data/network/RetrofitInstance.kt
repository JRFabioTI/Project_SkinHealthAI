package com.example.skinhealthai.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton para manter uma única instância do Retrofit
object RetrofitInstance {

    // URL base da sua API (10.0.2.2 é o localhost do emulador Android)
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Configuração do cliente OkHttp com interceptor para logging
    private val client by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY) // Log completo para debug
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Instância Retrofit para consumir a API (cria o ApiService)
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Converte JSON automaticamente
            .build()
            .create(ApiService::class.java)
    }
}
