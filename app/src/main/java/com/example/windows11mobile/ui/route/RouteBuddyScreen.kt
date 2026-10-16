package com.example.windows11mobile.ui.route

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
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
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
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Column {
                            Text(
                                text = "Route Buddy",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Smart Navigation & Transit Companion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // ROUTE FINDER CARD
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

                        // Origin Field
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

                        // Destination Field
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

                        // Travel Mode Selector
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

                        // Navigation Launch Button
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

            // NEARBY EXPLORER CATEGORIES
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

            // SAVED DESTINATIONS
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

            // LIVE TRAFFIC / TRANSIT CARD
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
}
