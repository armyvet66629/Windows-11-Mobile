package com.example.windows11mobile.ui.people

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.windows11mobile.ui.components.FluentSurface
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.data.Contact
import com.example.windows11mobile.data.RecentActivity
import com.example.windows11mobile.data.ActivityType
import com.example.windows11mobile.ui.components.FluentIcon
import com.example.windows11mobile.ui.theme.FluentIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeopleHubScreen(
    viewModel: PeopleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val recentActivity by viewModel.recentActivity.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    
    val favorites = remember(contacts) { contacts.filter { it.isStarred } }
    val searchResults = remember(contacts, searchQuery) {
        if (searchQuery.isEmpty()) emptyList()
        else contacts.filter { !it.isStarred }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Refresh if ANY permission is granted (Contacts, Calls, or SMS)
        if (permissions.values.any { it }) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = mutableListOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_SMS
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requiredPermissions.add(android.Manifest.permission.READ_PHONE_STATE)
        }
        permissionLauncher.launch(requiredPermissions.toTypedArray())
        viewModel.refresh() // Manual refresh on entry
    }

    var showDebug by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp, 
                bottom = 120.dp
            )
        ) {
            item {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    alpha = 0.5f,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = 150,
                    tintColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Color(0xFFB0B0B0).copy(alpha = 0.25f),
                    luminosityAlpha = 0.15f
                ) {
                    Column(modifier = Modifier.padding(24.dp).clickable { showDebug = !showDebug }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "People",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f),
                                letterSpacing = (-2).sp
                            )
                            IconButton(
                                onClick = { 
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Rounded.Dialpad, contentDescription = "Dialer", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(
                            "Your Social Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Search Bar
            item {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                FluentSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    alpha = 0.4f,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = 80,
                    tintColor = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFB0B0B0).copy(alpha = 0.2f),
                    borderAlpha = 0.2f
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                "Find someone...", 
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        },
                        leadingIcon = {
                            FluentIcon(
                                FluentIcons.Search,
                                contentDescription = null,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )
                }
            }

            if (showDebug) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DEBUG INFO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Recent Items: ${recentActivity.size}")
                            Text("Contacts: ${contacts.size}")
                            Text("Last Sync: ${if (lastSyncTime > 0) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime)) else "Never"}")
                            Text("Call Log Permission: ${ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED}")
                            Text("SMS Permission: ${ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED}")
                            Button(
                                onClick = { 
                                    viewModel.refresh()
                                }, 
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Force Refresh")
                            }
                        }
                    }
                }
            }

            // Favorites Section
            if (favorites.isNotEmpty()) {
                item {
                    FluentSurface(
                        shape = RoundedCornerShape(12.dp),
                        alpha = 0.4f,
                        effect = FluentEffect.ACRYLIC,
                        blurRadius = 60,
                        tintColor = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.heightIn(max = 600.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false
                    ) {
                        items(favorites) { contact ->
                            PinnedContactCard(
                                contact = contact,
                                onToggleFavorite = { viewModel.toggleStarred(contact) }
                            )
                        }
                    }
                }
            }

            // Recent Activity Section
            item {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                FluentSurface(
                    shape = RoundedCornerShape(12.dp),
                    alpha = 0.4f,
                    effect = FluentEffect.ACRYLIC,
                    blurRadius = 60,
                    tintColor = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFB0B0B0).copy(alpha = 0.2f)
                ) {
                    Text(
                        "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            if (recentActivity.isEmpty()) {
                item {
                    Text(
                        "No recent calls or messages found. Please ensure Permissions for Call Logs and SMS are granted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                item {
                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    FluentSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        alpha = 0.4f,
                        effect = FluentEffect.ACRYLIC,
                        blurRadius = 120,
                        tintColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Color(0xFFB0B0B0).copy(alpha = 0.25f),
                        luminosityAlpha = 0.15f
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            recentActivity.forEachIndexed { index, activity ->
                                ActivityItem(activity)
                                if (index < recentActivity.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Results (Only show when searching)
            if (searchResults.isNotEmpty()) {
                item {
                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    FluentSurface(
                        shape = RoundedCornerShape(12.dp),
                        alpha = 0.4f,
                        effect = FluentEffect.ACRYLIC,
                        blurRadius = 60,
                        tintColor = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFB0B0B0).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Search results for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(searchResults) { contact ->
                    ContactItem(
                        contact = contact,
                        onToggleFavorite = { viewModel.toggleStarred(contact) }
                    )
                }
            }
        }

        // Floating Action Button for New Contact
        FloatingActionButton(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                    context.startActivity(intent)
                } catch (e: Exception) {}
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 120.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Contact")
        }
    }
}

@Composable
fun ActivityItem(activity: RecentActivity) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = if (activity.type == ActivityType.CALL) {
                        // Dial back the person
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activity.address}"))
                    } else {
                        // Open specific SMS thread
                        Intent(Intent.ACTION_VIEW).apply {
                            if (activity.data != null) {
                                setDataAndType(Uri.parse("content://mms-sms/conversations/${activity.data}"), "vnd.android-dir/mms-sms")
                            } else {
                                setDataAndType(Uri.parse("sms:${activity.address}"), "vnd.android-dir/mms-sms")
                            }
                        }
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general apps if specific URI fails
                    try {
                        val fallback = if (activity.type == ActivityType.CALL) {
                            Intent(Intent.ACTION_DIAL)
                        } else {
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_APP_MESSAGING)
                            }
                        }
                        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(fallback)
                    } catch (e2: Exception) {}
                }
            }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (activity.summary.contains("Missed", true)) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (activity.photoUri != null) {
                AsyncImage(
                    model = activity.photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = if (activity.type == ActivityType.CALL) Icons.Rounded.Phone else FluentIcons.Message,
                    contentDescription = null,
                    tint = if (activity.summary.contains("Missed", true)) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = activity.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(activity.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PinnedContactCard(
    contact: Contact, 
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contactColor = remember(contact.name) {
        val colors = listOf(
            Color(0xFF0078D4), Color(0xFF1DB954), Color(0xFFE91E63), 
            Color(0xFF9B59B6), Color(0xFFF39C12), Color(0xFFE67E22),
            Color(0xFF16A085), Color(0xFF2980B9), Color(0xFF8E44AD)
        )
        colors[Math.abs(contact.name.hashCode()) % colors.size]
    }

    FluentSurface(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.id)
                        intent.data = uri
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                onLongClick = onToggleFavorite
            ),
        shape = RoundedCornerShape(24.dp),
        alpha = 0.45f,
        effect = FluentEffect.ACRYLIC,
        blurRadius = 80,
        tintColor = contactColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(contactColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (contact.photoUri != null) {
                    AsyncImage(
                        model = contact.photoUri,
                        contentDescription = contact.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = contact.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = contactColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = contact.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ContactItem(
    contact: Contact,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.id)
                        intent.data = uri
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                onLongClick = onToggleFavorite
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = contact.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        if (contact.isStarred) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = "Favorite",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
