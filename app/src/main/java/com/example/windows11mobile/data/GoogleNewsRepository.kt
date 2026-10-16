package com.example.windows11mobile.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object GoogleNewsRepository {

    private const val GOOGLE_NEWS_RSS_URL = "https://news.google.com/rss?hl=en-US&gl=US&ceid=US:en"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val fallbackBackdrops = listOf(
        "https://images.unsplash.com/photo-1504384308090-c894fdcc538d",
        "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
        "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5",
        "https://images.unsplash.com/photo-1518770660439-4636190af475",
        "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab",
        "https://images.unsplash.com/photo-1507679799987-c73779587ccf"
    )

    suspend fun fetchGoogleNews(): List<NewsArticle> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(GOOGLE_NEWS_RSS_URL)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .header("Accept", "application/xml, text/xml, */*")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GoogleNewsRepository", "Google News request failed: ${response.code}")
                    return@withContext getGoogleNewsFallbacks()
                }

                val bodyStream = response.body?.byteStream()
                if (bodyStream != null) {
                    val articles = parseGoogleNewsXml(bodyStream)
                    if (articles.isNotEmpty()) return@withContext articles
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleNewsRepository", "Error fetching Google News", e)
        }

        return@withContext getGoogleNewsFallbacks()
    }

    private fun parseGoogleNewsXml(inputStream: InputStream): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            var inItem = false
            var title = ""
            var link = ""
            var pubDate = ""
            var description = ""
            var sourceName = "Google News"
            var imageUrl: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            inItem = true
                            title = ""
                            link = ""
                            pubDate = ""
                            description = ""
                            sourceName = "Google News"
                            imageUrl = null
                        } else if (inItem) {
                            val lowerTag = tagName.lowercase()
                            when {
                                lowerTag == "title" -> title = parser.nextText()
                                lowerTag == "link" -> link = parser.nextText()
                                lowerTag == "pubdate" -> pubDate = parser.nextText()
                                lowerTag == "description" -> description = parser.nextText()
                                lowerTag == "source" -> sourceName = parser.nextText()
                                lowerTag.contains("content") || lowerTag.contains("thumbnail") || lowerTag.contains("enclosure") || lowerTag.contains("image") -> {
                                    val urlAttr = parser.getAttributeValue(null, "url") ?: parser.getAttributeValue(null, "href")
                                    if (!urlAttr.isNullOrBlank()) imageUrl = urlAttr
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true) && inItem) {
                            inItem = false
                            if (imageUrl.isNullOrBlank() && description.contains("<img", ignoreCase = true)) {
                                val match = Regex("""src=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(description)
                                imageUrl = match?.groupValues?.getOrNull(1)
                            }

                            val cleanSnippet = description.replace(Regex("<[^>]*>"), "").trim()
                            val cleanTitle = if (title.contains(" - ")) title.substringBeforeLast(" - ").trim() else title
                            val finalSource = if (title.contains(" - ")) title.substringAfterLast(" - ").trim() else sourceName

                            if (cleanTitle.isNotBlank()) {
                                val finalImage = imageUrl.takeIf { !it.isNullOrBlank() } ?: fallbackBackdrops[Math.abs(cleanTitle.hashCode()) % fallbackBackdrops.size]
                                articles.add(
                                    NewsArticle(
                                        title = cleanTitle,
                                        description = cleanSnippet.ifBlank { cleanTitle },
                                        url = link,
                                        urlToImage = finalImage,
                                        publishedAt = parseDate(pubDate),
                                        source = NewsSource(id = "google-news", name = finalSource)
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("GoogleNewsRepository", "Error parsing Google News XML", e)
        }
        return articles
    }

    private fun parseDate(dateStr: String): String {
        if (dateStr.isBlank()) return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return try {
            val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            val parsed = format.parse(dateStr)
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(parsed ?: Date())
        } catch (_: Exception) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        }
    }

    private fun getGoogleNewsFallbacks(): List<NewsArticle> {
        return listOf(
            NewsArticle(
                title = "Google News & Top Stories",
                description = "Explore personalized news stories, trending topics, and live updates directly from Google News.",
                url = "https://news.google.com",
                urlToImage = fallbackBackdrops[0],
                publishedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                source = NewsSource(id = "google-news", name = "Google News")
            )
        )
    }
}
