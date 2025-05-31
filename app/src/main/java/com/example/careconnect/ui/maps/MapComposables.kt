package com.example.careconnect.ui.maps

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
// Temporarily commenting out OSMDroid imports to fix build
// import androidx.compose.ui.viewinterop.AndroidView
import com.example.careconnect.api.OverpassPlace

// import org.osmdroid.config.Configuration
// import org.osmdroid.tileprovider.tilesource.TileSourceFactory
// import org.osmdroid.util.GeoPoint
// import org.osmdroid.views.MapView
// import org.osmdroid.views.overlay.Marker
// import org.osmdroid.views.overlay.infowindow.InfoWindow

@Composable
fun EmbeddedMapView(
    modifier: Modifier = Modifier,
    center: Pair<Double, Double>,
    places: List<OverpassPlace>,
    selectedPlaceType: String,
    onFullScreenClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            // Filter places based on selected type
            val filteredPlaces = when (selectedPlaceType) {
                "pharmacy" -> places.filter {
                    it.amenity == "pharmacy" || it.tags?.get("shop") == "chemist"
                }
                "clinic" -> places.filter {
                    it.amenity in listOf("clinic", "hospital", "doctors", "dentist") ||
                            it.tags?.get("healthcare") in listOf(
                        "clinic", "hospital", "doctor", "centre", "dentist"
                    )
                }
                else -> places
            }

            // Temporary placeholder until OSMDroid is properly configured
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Map",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Interactive Map View",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Center: ${
                            String.format(
                                "%.4f",
                                center.first
                            )
                        }, ${String.format("%.4f", center.second)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Showing ${filteredPlaces.size} markers",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Map loading...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Full screen button
            IconButton(
                onClick = onFullScreenClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Full Screen",
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenMapView(
    center: Pair<Double, Double>,
    places: List<OverpassPlace>,
    selectedPlaceType: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Filter places based on selected type
        val filteredPlaces = when (selectedPlaceType) {
            "pharmacy" -> places.filter {
                it.amenity == "pharmacy" || it.tags?.get("shop") == "chemist"
            }
            "clinic" -> places.filter {
                it.amenity in listOf("clinic", "hospital", "doctors", "dentist") ||
                        it.tags?.get("healthcare") in listOf(
                    "clinic", "hospital", "doctor", "centre", "dentist"
                )
            }
            else -> places
        }

        // Temporary placeholder for full screen map
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Map",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Full Screen Map View",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Center: ${String.format("%.4f", center.first)}, ${
                        String.format(
                            "%.4f",
                            center.second
                        )
                    }",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Would show ${filteredPlaces.size} markers",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Map loading...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
