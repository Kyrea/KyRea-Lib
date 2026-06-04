package com.example.rea

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface OpenLibraryApi {
    // YENİ: limit parametresi eklendi (varsayılan 20)
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): BookSearchResponse

    @GET
    suspend fun getBookDetails(@Url url: String): BookDetailsResponse
}