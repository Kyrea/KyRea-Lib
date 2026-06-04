package com.example.rea

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://openlibrary.org/"

    // lazy: Bu değişkene sadece ilk çağrıldığında hafızada yer ayrılır
    val apiService: OpenLibraryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> Kotlin dönüşüm motoru
            .build()
            .create(OpenLibraryApi::class.java)
    }
}