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
                Log.d("RssRepository", "Fetching feed: $url")
                val articles = fetchFeed(url)
                Log.d("RssRepository", "Found ${articles.size} articles in $url")
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
            val articles = parseRss(body.byteStream(), url)
            
            // Try to find missing images for the first few articles to keep it snappy
            return articles.mapIndexed { index, article ->
                if (article.urlToImage == null && index < 5) {
                    article.copy(urlToImage = fetchOpenGraphImage(article.url))
                } else {
                    article
                }
            }
        }
    }

    private fun fetchOpenGraphImage(url: String): String? {
        return try {
            val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val html = response.body?.string() ?: return null
                
                // Look for og:image or twitter:image
                val ogImage = Regex("<meta [^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
                    ?: Regex("<meta [^>]*content=[\"']([^\"']+)[\"'][^>]*property=[\"']og:image[\"']", RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
                
                val twitterImage = if (ogImage == null) {
                    Regex("<meta [^>]*name=[\"']twitter:image[\"'][^>]*content=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
                } else null
                
                ogImage ?: twitterImage
            }
        } catch (e: Exception) {
            null
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
        
        var sourceName = feedUrl.substringAfter("://").substringBefore("/")
        if (sourceName.startsWith("www.")) sourceName = sourceName.substring(4)
        sourceName = sourceName.capitalize(Locale.getDefault())

        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when {
                            name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true) -> {
                                currentTitle = null
                                currentLink = null
                                currentDescription = null
                                currentPubDate = null
                                currentImageUrl = null
                            }
                            name.equals("title", ignoreCase = true) -> currentTitle = parser.nextText()
                            name.equals("link", ignoreCase = true) -> {
                                val rel = parser.getAttributeValue(null, "rel")
                                if (rel == "alternate" || rel == null) {
                                    val href = parser.getAttributeValue(null, "href")
                                    currentLink = href ?: parser.nextText()
                                }
                            }
                            name.equals("description", ignoreCase = true) || name.equals("summary", ignoreCase = true) || name.equals("content:encoded", ignoreCase = true) -> {
                                val text = parser.nextText()
                                if (currentDescription == null || name.equals("description", ignoreCase = true)) {
                                    currentDescription = cleanHtml(text)
                                }
                                if (currentImageUrl == null) {
                                    currentImageUrl = extractFirstImageUrl(text)
                                }
                            }
                            name.equals("pubDate", ignoreCase = true) || name.equals("published", ignoreCase = true) || name.equals("updated", ignoreCase = true) -> {
                                currentPubDate = parser.nextText()
                            }
                            name.equals("enclosure", ignoreCase = true) -> {
                                val type = parser.getAttributeValue(null, "type")
                                if (type?.startsWith("image/") == true) {
                                    currentImageUrl = parser.getAttributeValue(null, "url")
                                }
                            }
                            name.equals("media:content", ignoreCase = true) || name.equals("content", ignoreCase = true) || name.equals("media:thumbnail", ignoreCase = true) || name.equals("media:group", ignoreCase = true) -> {
                                val url = parser.getAttributeValue(null, "url")
                                val type = parser.getAttributeValue(null, "type")
                                val medium = parser.getAttributeValue(null, "medium")
                                
                                if (url != null) {
                                    if (currentImageUrl == null || medium == "image" || type?.startsWith("image/") == true) {
                                        currentImageUrl = url
                                    }
                                } else if (name.equals("media:group", ignoreCase = true)) {
                                    // Deep dive into media:group if needed, but parser.next() might be better
                                }
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
        } catch (e: Exception) {
            Log.e("RssRepository", "XML Parse Error in $feedUrl: ${e.message}")
        }
        return articles
    }

    private fun cleanHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").trim()
    }

    private fun extractFirstImageUrl(html: String): String? {
        val pattern = Regex("<img [^>]*src=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
        return pattern.find(html)?.groupValues?.get(1)
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
