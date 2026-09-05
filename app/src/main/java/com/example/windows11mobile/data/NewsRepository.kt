package com.example.windows11mobile.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsRepository {
    suspend fun getTopHeadlines(categories: Set<String> = emptySet()): List<NewsArticle>
}

class RealNewsRepository(private val apiKey: String?) : NewsRepository {
    
    private val service = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(NewsApiService::class.java)

    override suspend fun getTopHeadlines(categories: Set<String>): List<NewsArticle> {
        if (apiKey.isNullOrBlank()) return getMockArticles()
        
        return try {
            if (categories.isEmpty()) {
                val response = service.getTopHeadlines(apiKey = apiKey)
                return if (response.status == "ok") response.articles else getMockArticles()
            }

            val allArticles = mutableListOf<NewsArticle>()
            categories.take(3).forEach { category -> // Limit to 3 categories to avoid too many requests
                val response = service.getTopHeadlines(category = category, apiKey = apiKey)
                if (response.status == "ok") {
                    allArticles.addAll(response.articles)
                }
            }
            
            if (allArticles.isEmpty()) {
                getMockArticles()
            } else {
                allArticles.sortedByDescending { it.publishedAt }.distinctBy { it.url }
            }
        } catch (e: Exception) {
            getMockArticles()
        }
    }

    private fun getMockArticles(): List<NewsArticle> = emptyList()
}

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
