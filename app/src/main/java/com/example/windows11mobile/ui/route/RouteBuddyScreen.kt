package com.example.windows11mobile.ui.route

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.windows11mobile.ui.components.FluentEffect
import com.example.windows11mobile.ui.components.FluentSurface
import java.util.UUID

enum class MailType(val displayName: String, val color: Color, val icon: ImageVector) {
    ACTIVE_FORWARD("Active Forward", Color(0xFF43A047), Icons.Rounded.CheckCircle),
    TEMPORARY_FORWARD("Temporary Forward", Color(0xFFFBC02D), Icons.Rounded.DateRange),
    CUSTOMER_HOLD("Customer Hold", Color(0xFF1976D2), Icons.Rounded.Info),
    UNABLE_TO_FORWARD("Unable to Forward", Color(0xFFD32F2F), Icons.Rounded.Warning),
    DECEASED("Deceased", Color(0xFF888888), Icons.Rounded.Person)
}

data class ForwardableMail(
    val id: String,
    val recipient: String,
    val address: String,
    val startDate: String,
    val endDate: String? = null,
    val notes: String? = null,
    val type: MailType = MailType.ACTIVE_FORWARD
)

data class SavedRoute(
    val id: String,
    val title: String,
    val address: String,
    val icon: ImageVector,
    val category: String,
    val estimatedMinutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteBuddyScreen(
    onOpenNavigation: (destination: String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var originInput by remember { mutableStateOf("Current Location") }
    var destinationInput by remember { mutableStateOf("") }
    var selectedTravelMode by remember { mutableStateOf("Drive") }

    val savedRoutes = remember {
        listOf(
            SavedRoute("1", "Home", "742 Evergreen Terrace", Icons.Rounded.Home, "Personal", 12),
            SavedRoute("2", "Work / Office", "100 Infinite Loop, Cupertino", Icons.Rounded.Work, "Work", 24),
            SavedRoute("3", "Central Park Gym", "830 5th Ave, New York", Icons.Rounded.FitnessCenter, "Fitness", 18),
            SavedRoute("4", "Whole Foods Market", "250 Seventh Ave", Icons.Rounded.ShoppingCart, "Shopping", 9)
        )
    }

    val quickSearchCategories = listOf(
        Triple("Gas", Icons.Rounded.LocalGasStation, "gas station"),
        Triple("Food", Icons.Rounded.Restaurant, "restaurants"),
        Triple("Coffee", Icons.Rounded.Coffee, "coffee shop"),
        Triple("Parking", Icons.Rounded.LocalParking, "parking lot"),
        Triple("Transit", Icons.Rounded.DirectionsBus, "transit station"),
        Triple("Ev Charge", Icons.Rounded.EvStation, "ev charging")
    )

    var mailSearchQuery by remember { mutableStateOf("") }
    var selectedMailTypeFilter by remember { mutableStateOf<MailType?>(null) }
    var showAddMailDialog by remember { mutableStateOf(false) }

    var mailItems by remember {
        mutableStateOf(
            listOf(
                ForwardableMail("1", "John Smith", "104 Maple Ave, Suite 2B", "10/01/2026", "11/15/2026", "Forwarding all first-class mail", MailType.ACTIVE_FORWARD),
                ForwardableMail("2", "Robert Vance", "402 Oak Street", "09/20/2026", "10/20/2026", "Vacation customer hold", MailType.CUSTOMER_HOLD),
                ForwardableMail("3", "Emily Watson", "88 Pine Road", "10/05/2026", "10/25/2026", "Temporary forwarding to summer cottage", MailType.TEMPORARY_FORWARD),
                ForwardableMail("4", "David Miller", "512 Elm Street", "08/12/2026", null, "Moved - No forwarding address on file", MailType.UNABLE_TO_FORWARD)
            )
        )
    }

    val filteredMailItems = remember(mailItems, mailSearchQuery, selectedMailTypeFilter) {
        mailItems.filter { item ->
            val matchesFilter = selectedMailTypeFilter == null || item.type == selectedMailTypeFilter
            val matchesSearch = mailSearchQuery.isBlank() ||
                    item.recipient.contains(mailSearchQuery, ignoreCase = true) ||
                    item.address.contains(mailSearchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            FluentSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                alpha = 0.45f,
                effect = FluentEffect.ACRYLIC,
                blurRadius = 100,
                tintColor = Color.Black.copy(alpha = 0.3f),
                borderAlpha = 0.15f
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Navigation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Route Buddy",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Postal Forwarding & Navigation Companion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "ROUTES & MAPS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 1.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "POSTAL & FORWARDING",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 1.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddMailDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 64.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Forwarding")
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            label = "tabCrossfade"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        item {
                            FluentSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                alpha = 0.35f,
                                effect = FluentEffect.ACRYLIC,
                                blurRadius = 100,
                                tintColor = Color.Black.copy(alpha = 0.25f),
                                borderAlpha = 0.15f
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "PLAN A ROUTE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.2.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = originInput,
                                        onValueChange = { originInput = it },
                                        label = { Text("Start Location") },
                                        leadingIcon = { Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = destinationInput,
                                        onValueChange = { destinationInput = it },
                                        label = { Text("Where to?") },
                                        placeholder = { Text("Enter address, place, or city...") },
                                        leadingIcon = { Icon(Icons.Rounded.Place, contentDescription = null, tint = Color(0xFFE74C3C)) },
                                        trailingIcon = {
                                            if (destinationInput.isNotBlank()) {
                                                IconButton(onClick = { destinationInput = "" }) {
                                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            Triple("Drive", Icons.Rounded.DirectionsCar, "driving"),
                                            Triple("Transit", Icons.Rounded.DirectionsBus, "transit"),
                                            Triple("Walk", Icons.AutoMirrored.Rounded.DirectionsWalk, "walking")
                                        ).forEach { (mode, icon, _) ->
                                            val isSelected = selectedTravelMode == mode
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedTravelMode = mode },
                                                label = { Text(mode) },
                                                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            val destination = destinationInput.ifBlank { "gas station" }
                                            onOpenNavigation(destination)
                                        },
                                        enabled = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Icon(Icons.Rounded.NearMe, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (destinationInput.isNotBlank()) "Start Navigation" else "Find Nearby Places",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column {
                                Text(
                                    text = "NEARBY EXPLORER",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(quickSearchCategories) { (title, icon, query) ->
                                        FluentSurface(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .clickable { onOpenNavigation(query) },
                                            shape = RoundedCornerShape(16.dp),
                                            alpha = 0.35f,
                                            effect = FluentEffect.ACRYLIC,
                                            blurRadius = 80,
                                            tintColor = Color.Black.copy(alpha = 0.25f),
                                            borderAlpha = 0.15f
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = title,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "SAVED DESTINATIONS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(savedRoutes, key = { it.id }) { route ->
                            FluentSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenNavigation(route.address) },
                                shape = RoundedCornerShape(16.dp),
                                alpha = 0.35f,
                                effect = FluentEffect.ACRYLIC,
                                blurRadius = 80,
                                tintColor = Color.Black.copy(alpha = 0.25f),
                                borderAlpha = 0.15f
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = route.icon,
                                            contentDescription = route.title,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = route.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = route.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "~${route.estimatedMinutes}m",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                        contentDescription = "Navigate",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        item {
                            FluentSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                alpha = 0.35f,
                                effect = FluentEffect.ACRYLIC,
                                blurRadius = 100,
                                tintColor = Color.Black.copy(alpha = 0.25f),
                                borderAlpha = 0.15f
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2ECC71).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Traffic,
                                            contentDescription = null,
                                            tint = Color(0xFF2ECC71),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Traffic Conditions Normal",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Routes around your area are flowing smoothly",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "POSTAL DASHBOARD",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val activeCount = mailItems.count { it.type == MailType.ACTIVE_FORWARD }
                                    val tempCount = mailItems.count { it.type == MailType.TEMPORARY_FORWARD }
                                    val holdCount = mailItems.count { it.type == MailType.CUSTOMER_HOLD }

                                    listOf(
                                        Triple("Active", activeCount, MailType.ACTIVE_FORWARD),
                                        Triple("Temp", tempCount, MailType.TEMPORARY_FORWARD),
                                        Triple("Holds", holdCount, MailType.CUSTOMER_HOLD)
                                    ).forEach { (label, count, type) ->
                                        val isFilterActive = selectedMailTypeFilter == type
                                        FluentSurface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedMailTypeFilter = if (isFilterActive) null else type
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            alpha = if (isFilterActive) 0.6f else 0.35f,
                                            effect = FluentEffect.ACRYLIC,
                                            blurRadius = 80,
                                            tintColor = if (isFilterActive) type.color.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.25f),
                                            borderAlpha = 0.2f
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = type.color,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "$count",
                                                    style = MaterialTheme.typography.headlineLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = mailSearchQuery,
                                    onValueChange = { mailSearchQuery = it },
                                    placeholder = { Text("Search recipient or address...") },
                                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (mailSearchQuery.isNotBlank()) {
                                            IconButton(onClick = { mailSearchQuery = "" }) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                if (selectedMailTypeFilter != null) {
                                    IconButton(
                                        onClick = { selectedMailTypeFilter = null },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Rounded.FilterAltOff, contentDescription = "Clear filter", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "FORWARDING & HOLD ENTRIES (${filteredMailItems.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(filteredMailItems, key = { it.id }) { item ->
                            FluentSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                alpha = 0.35f,
                                effect = FluentEffect.ACRYLIC,
                                blurRadius = 80,
                                tintColor = Color.Black.copy(alpha = 0.25f),
                                borderAlpha = 0.15f
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.recipient,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = item.type.color.copy(alpha = 0.2f),
                                            contentColor = item.type.color
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(item.type.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.type.displayName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (!item.notes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Start: ${item.startDate}${if (item.endDate != null) " • End: ${item.endDate}" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        IconButton(
                                            onClick = {
                                                mailItems = mailItems.filter { it.id != item.id }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFE74C3C), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMailDialog) {
        var newRecipient by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }
        var newNotes by remember { mutableStateOf("") }
        var newType by remember { mutableStateOf(MailType.ACTIVE_FORWARD) }

        AlertDialog(
            onDismissRequest = { showAddMailDialog = false },
            title = { Text("Add Postal Forwarding Entry", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newRecipient,
                        onValueChange = { newRecipient = it },
                        label = { Text("Recipient Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Forwarding Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes / Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Mail Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    MailType.entries.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newType = type }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = newType == type,
                                onClick = { newType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(type.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRecipient.isNotBlank() && newAddress.isNotBlank()) {
                            val newItem = ForwardableMail(
                                id = UUID.randomUUID().toString(),
                                recipient = newRecipient,
                                address = newAddress,
                                startDate = "10/06/2026",
                                endDate = "11/06/2026",
                                notes = newNotes.ifBlank { null },
                                type = newType
                            )
                            mailItems = listOf(newItem) + mailItems
                            showAddMailDialog = false
                        }
                    },
                    enabled = newRecipient.isNotBlank() && newAddress.isNotBlank()
                ) {
                    Text("Add Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
