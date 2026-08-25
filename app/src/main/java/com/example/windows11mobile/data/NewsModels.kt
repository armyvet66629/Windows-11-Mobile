package com.example.windows11mobile.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticle>
)

@JsonClass(generateAdapter = true)
data class NewsArticle(
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val source: NewsSource
)

@JsonClass(generateAdapter = true)
data class NewsSource(
    val id: String?,
    val name: String
)
