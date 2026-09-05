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

import java.util.concurrent.TimeUnit

class RssRepository(private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()) {

    suspend fun fetchFeeds(urls: Set<String>): List<NewsArticle> = withContext(Dispatchers.IO) {
        android.util.Log.d("RssRepository", "fetchFeeds starting with ${urls.size} URLs: $urls")
        val allArticles = mutableListOf<NewsArticle>()
        urls.forEach { url ->
            try {
                Log.d("RssRepository", "Processing URL: $url")
                val articles = fetchFeed(url)
                Log.d("RssRepository", "Successfully fetched ${articles.size} articles from $url")
                allArticles.addAll(articles)
            } catch (e: Exception) {
                Log.e("RssRepository", "Error fetching feed: $url", e)
            }
        }
        val result = allArticles.sortedByDescending { it.publishedAt }
        android.util.Log.d("RssRepository", "fetchFeeds complete. Total articles: ${result.size}")
        result
    }

    private fun fetchFeed(url: String): List<NewsArticle> {
        android.util.Log.d("RssRepository", "fetchFeed: $url")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .header("Accept", "application/xml,application/rss+xml,application/atom+xml,text/xml;q=0.9")
            .header("Cache-Control", "no-cache")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                android.util.Log.d("RssRepository", "Response for $url: ${response.code}")
                if (!response.isSuccessful) {
                    Log.e("RssRepository", "Feed response failed for $url: ${response.code} ${response.message}")
                    return emptyList()
                }
                
                val body = response.body
                if (body == null) {
                    Log.e("RssRepository", "Empty body for $url")
                    return emptyList()
                }

                // Check content type
                val contentType = response.header("Content-Type")
                android.util.Log.d("RssRepository", "Content-Type for $url: $contentType")

                val bytes = body.bytes()
                if (bytes.size < 500) {
                    val content = String(bytes)
                    android.util.Log.d("RssRepository", "Short body content for $url: $content")
                } else {
                    val snippet = String(bytes.take(500).toByteArray())
                    android.util.Log.d("RssRepository", "Body snippet for $url: $snippet")
                }

                val articles = parseRss(bytes.inputStream(), url)
                android.util.Log.d("RssRepository", "Parsed ${articles.size} articles from $url")
                
                // Try to find missing images for the first few articles to keep it snappy
                articles.mapIndexed { index, article ->
                    if (article.urlToImage == null && index < 3) {
                        article.copy(urlToImage = fetchOpenGraphImage(article.url))
                    } else {
                        article
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RssRepository", "Network Error fetching feed: $url", e)
            emptyList()
        }
    }

    private fun fetchOpenGraphImage(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
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
        sourceName = sourceName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

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
                            name.equals("title", ignoreCase = true) -> currentTitle = collectText(parser)
                            name.equals("link", ignoreCase = true) -> {
                                val href = parser.getAttributeValue(null, "href")
                                if (href != null) {
                                    currentLink = href
                                } else {
                                    currentLink = collectText(parser)
                                }
                            }
                            name.contains("description", ignoreCase = true) || 
                            name.contains("summary", ignoreCase = true) || 
                            name.contains("content", ignoreCase = true) || 
                            name.contains("encoded", ignoreCase = true) -> {
                                val text = collectText(parser)
                                if (currentDescription == null || name.contains("description", ignoreCase = true) || name.contains("summary", ignoreCase = true)) {
                                    currentDescription = if (text.isNotBlank()) cleanHtml(text) else currentDescription
                                }
                                if (currentImageUrl == null) {
                                    currentImageUrl = extractFirstImageUrl(text)
                                }
                            }
                            name.contains("pubDate", ignoreCase = true) || 
                            name.contains("published", ignoreCase = true) || 
                            name.contains("updated", ignoreCase = true) || 
                            name.contains("date", ignoreCase = true) -> {
                                currentPubDate = collectText(parser)
                            }
                            name.equals("enclosure", ignoreCase = true) -> {
                                val type = parser.getAttributeValue(null, "type")
                                if (type?.startsWith("image/") == true || currentImageUrl == null) {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (url != null) currentImageUrl = url
                                }
                            }
                            name.contains("thumbnail", ignoreCase = true) || 
                            (name.contains("content", ignoreCase = true) && parser.getAttributeValue(null, "url") != null) -> {
                                val url = parser.getAttributeValue(null, "url")
                                if (url != null && (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".jpeg") || url.contains("image"))) {
                                    currentImageUrl = url
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name.equals("item", ignoreCase = true) || name.equals("entry", ignoreCase = true)) {
                            if (!currentTitle.isNullOrBlank() && !currentLink.isNullOrBlank()) {
                                articles.add(
                                    NewsArticle(
                                        title = currentTitle!!.trim(),
                                        description = currentDescription?.take(400),
                                        url = currentLink!!,
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

    private fun collectText(parser: XmlPullParser): String {
        val sb = StringBuilder()
        try {
            var eventType = parser.next()
            while (eventType != XmlPullParser.END_TAG && eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT || eventType == XmlPullParser.CDSECT) {
                    sb.append(parser.text)
                } else if (eventType == XmlPullParser.START_TAG) {
                    // Nested tag? Skip it but keep the text
                    sb.append(collectText(parser))
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        return sb.toString().trim()
    }

    private fun cleanHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .trim()
    }

    private fun extractFirstImageUrl(html: String): String? {
        // 1. Look for <img> tags
        val pattern = Regex("<img [^>]*src=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
        val imgMatch = pattern.find(html)?.groupValues?.get(1)
        if (imgMatch != null && !imgMatch.startsWith("data:")) return imgMatch
        
        // 2. Look for background-image: url(...)
        val bgPattern = Regex("url\\(['\"]?([^'\")]+)['\"]?\\)", RegexOption.IGNORE_CASE)
        val bgMatch = bgPattern.find(html)?.groupValues?.get(1)
        
        return bgMatch
    }

    private fun formatPubDate(pubDate: String?): String {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        if (pubDate.isNullOrBlank()) return now
        
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        
        for (format in formats) {
            try {
                val date = SimpleDateFormat(format, Locale.US).parse(pubDate.trim())
                if (date != null) {
                    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(date)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        return if (pubDate.length > 10 && pubDate[4] == '-' && pubDate[7] == '-') pubDate else now
    }
}
