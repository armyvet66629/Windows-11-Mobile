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

    private fun getMockArticles(): List<NewsArticle> {
        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(calendar.time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return listOf(
            NewsArticle(
                title = "Microsoft Surface Duo 3: New Leaks Suggest Major Design Shift",
                description = "Internal sources claim Microsoft is moving towards a more traditional foldable screen for its next mobile device.",
                url = "https://microsoft.com/surface/1",
                urlToImage = "https://images.unsplash.com/photo-1633419461186-7d40a38105ec",
                publishedAt = today,
                source = NewsSource("ms", "Microsoft News")
            ),
            NewsArticle(
                title = "Android 15 Features: Everything We Know So Far",
                description = "Google's upcoming update focuses heavily on privacy and edge-to-edge app experiences.",
                url = "https://developer.android.com/android15",
                urlToImage = "https://images.unsplash.com/photo-1607252658945-978a91f958b9",
                publishedAt = today,
                source = NewsSource("google", "Android Developers")
            ),
            NewsArticle(
                title = if (hour % 2 == 0) "Fluent Design: Building the Next Generation of Apps" else "Windows 11 Mobile Concept Gaining Traction",
                description = "Developers are finding new ways to bring the Windows 11 aesthetic to portable devices.",
                url = "https://github.com/windows11concept",
                urlToImage = "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c",
                publishedAt = today,
                source = NewsSource("dev", "Dev Community")
            )
        )
    }
}

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
