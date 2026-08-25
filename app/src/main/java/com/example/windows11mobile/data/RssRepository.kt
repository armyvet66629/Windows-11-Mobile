package com.example.windows11mobile.data

import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class RssRepository(private val client: OkHttpClient = OkHttpClient()) {

    suspend fun fetchFeeds(urls: Set<String>): List<NewsArticle> = withContext(Dispatchers.IO) {
        val allArticles = mutableListOf<NewsArticle>()
        urls.forEach { url ->
            try {
                val articles = fetchFeed(url)
                allArticles.addAll(articles)
            } catch (e: Exception) {
                Log.e("RssRepository", "Error fetching feed: $url", e)
            }
        }
        allArticles.sortedByDescending { it.publishedAt }
    }

    private fun fetchFeed(url: String): List<NewsArticle> {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            val body = response.body ?: return emptyList()
            return parseRss(body.byteStream(), url)
        }
    }

    private fun parseRss(inputStream: InputStream, feedUrl: String): List<NewsArticle> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        
        val articles = mutableListOf<NewsArticle>()
        var eventType = parser.eventType
        
        var currentTitle: String? = null
        var currentLink: String? = null
        var currentDescription: String? = null
        var currentPubDate: String? = null
        var currentImageUrl: String? = null
        
        val sourceName = feedUrl.substringAfter("://").substringBefore("/")

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                        currentTitle = null
                        currentLink = null
                        currentDescription = null
                        currentPubDate = null
                        currentImageUrl = null
                    } else if (name.equals("title", ignoreCase = true)) {
                        currentTitle = parser.nextText()
                    } else if (name.equals("link", ignoreCase = true)) {
                        val rel = parser.getAttributeValue(null, "rel")
                        if (rel == "alternate" || rel == null) {
                            val href = parser.getAttributeValue(null, "href")
                            currentLink = href ?: parser.nextText()
                        }
                    } else if (name.equals("description", ignoreCase = true) || name.equals("summary", ignoreCase = true)) {
                        currentDescription = parser.nextText()
                    } else if (name.equals("pubDate", ignoreCase = true) || name.equals("published", ignoreCase = true) || name.equals("updated", ignoreCase = true)) {
                        currentPubDate = parser.nextText()
                    } else if (name.equals("enclosure", ignoreCase = true)) {
                        val type = parser.getAttributeValue(null, "type")
                        if (type?.startsWith("image/") == true) {
                            currentImageUrl = parser.getAttributeValue(null, "url")
                        }
                    } else if (name.equals("media:content", ignoreCase = true) || name.equals("content", ignoreCase = true)) {
                        val url = parser.getAttributeValue(null, "url")
                        if (url != null) {
                            currentImageUrl = url
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                        if (currentTitle != null && currentLink != null) {
                            articles.add(
                                NewsArticle(
                                    title = currentTitle,
                                    description = currentDescription?.let { cleanHtml(it) },
                                    url = currentLink,
                                    urlToImage = currentImageUrl,
                                    publishedAt = formatPubDate(currentPubDate),
                                    source = NewsSource(id = "rss", name = sourceName)
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return articles
    }

    private fun cleanHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").trim()
    }

    private fun formatPubDate(pubDate: String?): String {
        if (pubDate == null) return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        )
        
        for (format in formats) {
            try {
                val date = SimpleDateFormat(format, Locale.US).parse(pubDate)
                if (date != null) {
                    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(date)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        return pubDate // Fallback to original string if parsing fails
    }
}
