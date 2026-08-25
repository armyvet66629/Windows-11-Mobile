package com.example.windows11mobile.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.windows11mobile.data.NewsArticle
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.ui.components.FluentSurface
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.windows11mobile.ui.theme.FluentIcons
import com.example.windows11mobile.ui.components.FluentIcon

@Composable
fun NewsFeedScreen(
    viewModel: NewsFeedViewModel,
    modifier: Modifier = Modifier
) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val preferredCategories by viewModel.preferredCategories.collectAsStateWithLifecycle()
    val rssFeeds by viewModel.rssFeeds.collectAsStateWithLifecycle()
    val tileOpacity by viewModel.tileOpacity.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    
    var showCustomizeDialog by remember { mutableStateOf(false) }

    if (showCustomizeDialog) {
        CustomizeFeedDialog(
            selectedCategories = preferredCategories,
            rssFeeds = rssFeeds,
            onDismiss = { showCustomizeDialog = false },
            onSaveCategories = { categories ->
                viewModel.updateCategories(categories)
            },
            onAddRssFeed = { url ->
                viewModel.addRssFeed(url)
            },
            onRemoveRssFeed = { url ->
                viewModel.removeRssFeed(url)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        item {
            NewsHeader(
                onCustomizeClick = { showCustomizeDialog = true },
                onRefreshClick = { viewModel.refresh() }
            )
        }

        // API Key Tip
        item {
            FluentSurface(
                modifier = Modifier
                    .fillMaxWidth(),
                alpha = 0.3f,
                effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
                blurRadius = 15,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Tip: Add a NewsAPI.org key in RealNewsRepository to see live news.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }

        if (isLoading && articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(articles) { article ->
                NewsCard(
                    article = article,
                    tileOpacity = tileOpacity,
                    onClick = { uriHandler.openUri(article.url) }
                )
            }
        }
    }
}

@Composable
fun NewsCard(
    article: NewsArticle,
    tileOpacity: Float = 0.6f,
    onClick: () -> Unit
) {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        alpha = tileOpacity.coerceAtLeast(0.6f), // Ensure legibility in light mode
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 40
    ) {
        Column {
            if (article.urlToImage != null) {
                AsyncImage(
                    model = article.urlToImage,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Slightly taller image
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = article.source.name.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge, // Larger title
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (article.description != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = article.publishedAt.substringBefore("T"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CustomizeFeedDialog(
    selectedCategories: Set<String>,
    rssFeeds: Set<String>,
    onDismiss: () -> Unit,
    onSaveCategories: (Set<String>) -> Unit,
    onAddRssFeed: (String) -> Unit,
    onRemoveRssFeed: (String) -> Unit
) {
    val categories = listOf("business", "entertainment", "general", "health", "science", "sports", "technology")
    var tempSelected by remember { mutableStateOf(selectedCategories) }
    var newRssUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        FluentSurface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            alpha = 0.9f,
            effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
            blurRadius = 60
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Customize News Feed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "News Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelected = if (tempSelected.contains(category)) {
                                        tempSelected - category
                                    } else {
                                        tempSelected + category
                                    }
                                    onSaveCategories(tempSelected)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tempSelected.contains(category),
                                onCheckedChange = { checked ->
                                    tempSelected = if (checked) {
                                        tempSelected + category
                                    } else {
                                        tempSelected - category
                                    }
                                    onSaveCategories(tempSelected)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "RSS Feeds",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(rssFeeds.toList()) { url ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = url,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveRssFeed(url) }) {
                                Icon(
                                    FluentIcons.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newRssUrl,
                                onValueChange = { newRssUrl = it },
                                label = { Text("RSS Feed URL") },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newRssUrl.isNotBlank()) {
                                        onAddRssFeed(newRssUrl)
                                        newRssUrl = ""
                                    }
                                },
                                enabled = newRssUrl.isNotBlank()
                            ) {
                                Icon(FluentIcons.Widgets, contentDescription = "Add")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun NewsHeader(
    onCustomizeClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    var currentTime by remember { 
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) 
    }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(10000) // Update every 10 seconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp) // Added some bottom padding to balance with LazyColumn spacedBy
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary // Higher contrast in light mode
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Here's what's happening today.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground, // High contrast
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row {
                IconButton(onClick = onCustomizeClick) {
                    FluentIcon(
                        FluentIcons.Settings, 
                        contentDescription = "Customize",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onRefreshClick) {
                    FluentIcon(
                        FluentIcons.Widgets, // Use a refresh-like icon if possible
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Weather Widget Pill
        WeatherWidget()
    }
}

@Composable
fun WeatherWidget() {
    FluentSurface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        alpha = 0.7f, 
        effect = com.example.windows11mobile.ui.components.FluentEffect.ACRYLIC,
        blurRadius = 40,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp) // More padding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weather Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                FluentIcon(
                    imageVector = FluentIcons.Weather, 
                    contentDescription = null,
                    size = 40.dp,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "72°F",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black, // Extra bold
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sunny • Redmond, WA",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold // Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "H: 75° L: 62°",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AQI: 24 (Good)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NewsFeedScreenPreview() {
    val context = LocalContext.current
    val repository = remember { com.example.windows11mobile.data.RealNewsRepository(null) }
    val rssRepository = remember { com.example.windows11mobile.data.RssRepository() }
    val settingsRepository = remember { com.example.windows11mobile.data.RealSettingsRepository(context) }
    val viewModel = remember { NewsFeedViewModel(repository, rssRepository, settingsRepository) }
    
    com.example.windows11mobile.ui.theme.Windows11MobileTheme {
        NewsFeedScreen(viewModel = viewModel)
    }
}
