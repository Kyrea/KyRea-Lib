package com.example.rea

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BookSearchResponse(
    @SerializedName("docs") val docs: List<ApiBook>
)

data class ApiBook(
    @SerializedName("title") val title: String,
    @SerializedName("author_name") val authorNames: List<String>?,
    @SerializedName("cover_i") val coverId: Int?,
    @SerializedName("key") val key: String
)

data class BookDetailsResponse(
    @SerializedName("description") val description: JsonElement?
)