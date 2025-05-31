package com.example.skinhealthai.utils

import android.content.Context
import android.content.SharedPreferences

object AuthTokenManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"

    // Função para salvar o token
    fun saveAuthToken(context: Context, token: String) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString(KEY_AUTH_TOKEN, token)
            apply() // Use apply() para salvar de forma assíncrona
        }
    }

    // Função para obter o token
    fun getAuthToken(context: Context): String? {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_AUTH_TOKEN, null) // Retorna null se o token não for encontrado
    }

    // Função para remover o token (no logout)
    fun clearAuthToken(context: Context) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove(KEY_AUTH_TOKEN)
            apply()
        }
    }
}