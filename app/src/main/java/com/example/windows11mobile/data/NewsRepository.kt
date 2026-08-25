package com.example.windows11mobile.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    private fun getMockArticles(): List<NewsArticle> = listOf(
        NewsArticle(
            title = "Microsoft Announces Next Generation of Windows",
            description = "The new update brings a fresh look and improved productivity features to the world's most popular desktop OS.",
            url = "https://microsoft.com",
            urlToImage = "https://images.unsplash.com/photo-1633419461186-7d40a38105ec",
            publishedAt = "2026-08-21T10:00:00Z",
            source = NewsSource("ms", "Microsoft News")
        ),
        NewsArticle(
            title = "Jetpack Compose: The Future of Android UI",
            description = "Google continues to evolve its modern toolkit for building native Android UI.",
            url = "https://developer.android.com",
            urlToImage = "https://images.unsplash.com/photo-1607252658945-978a91f958b9",
            publishedAt = "2026-08-21T11:00:00Z",
            source = NewsSource("google", "Android Developers")
        ),
        NewsArticle(
            title = "Windows 11 Mobile: A Concept Realized",
            description = "What happens when you bring the Fluent Design system to a mobile form factor? This project explores the possibilities.",
            url = "https://github.com",
            urlToImage = "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c",
            publishedAt = "2026-08-21T12:00:00Z",
            source = NewsSource("gh", "GitHub Trends")
        ),
        NewsArticle(
            title = "New Surface Duo 3 Rumors Surfaces",
            description = "Leaks suggest a more integrated approach to the dual-screen experience with Android 15.",
            url = "https://windowscentral.com",
            urlToImage = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0",
            publishedAt = "2026-08-21T13:00:00Z",
            source = NewsSource("wc", "Windows Central")
        )
    )
}

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
