package com.example.skinhealthai.data.network

import android.content.Context // Importe Context
import com.example.skinhealthai.utils.AuthTokenManager // Importe seu AuthTokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Para definir timeouts, se quiser

// Singleton para manter uma única instância do Retrofit
object RetrofitInstance {

    // URL base da sua API (10.0.2.2 é o localhost do emulador Android)
    // Se você está usando 192.168.1.3, é provável que esteja testando em um dispositivo físico
    private const val BASE_URL = "http://192.168.1.3:8000/" // Adicione a barra final se não houver

    // Variável para armazenar o Context globalmente (com cuidado)
    // Inicialize-a em sua Application class (SkinHealthAIApp.kt)
    var applicationContext: Context? = null

    // Configuração do cliente OkHttp com interceptor para logging e token
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY) // Para ver os detalhes da requisição e resposta no Logcat
        }

        OkHttpClient.Builder()
            .addInterceptor(logging) // Adiciona o interceptor de logging para depuração
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // Tenta obter o token usando o Context
                applicationContext?.let { context ->
                    val authToken = AuthTokenManager.getAuthToken(context)
                    authToken?.let { token ->
                        // Adiciona o cabeçalho de Authorization se o token existir
                        // IMPORTANTE: Verifique no seu backend Django qual o prefixo do token.
                        // Se for Django REST Framework Token Authentication padrão, é 'Token '.
                        // Se for JWT, geralmente é 'Bearer '.
                        requestBuilder.header("Authorization", "Bearer $token") // OU "Token $token"
                    }
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS) // Exemplo de timeout de conexão
            .readTimeout(30, TimeUnit.SECONDS)    // Exemplo de timeout de leitura
            .writeTimeout(30, TimeUnit.SECONDS)   // Exemplo de timeout de escrita
            .build()
    }

    // Instância Retrofit para consumir a API (cria o ApiService)
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Usa o cliente OkHttp configurado com o interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}