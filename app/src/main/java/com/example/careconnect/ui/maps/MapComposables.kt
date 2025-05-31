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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.careconnect.api.OverpassPlace
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

@Composable
fun EmbeddedMapView(
    modifier: Modifier = Modifier,
    center: Pair<Double, Double>,
    places: List<OverpassPlace>,
    selectedPlaceType: String,
    onFullScreenClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var mapInitialized by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf(false) }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        try {
            Configuration.getInstance().apply {
                userAgentValue = "CareConnect/1.0"
                osmdroidTileCache = context.cacheDir
            }
            mapInitialized = true
        } catch (e: Exception) {
            mapError = true
        }
    }

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

            if (mapInitialized && !mapError) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        try {
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(14.0)
                                controller.setCenter(GeoPoint(center.first, center.second))

                                // Add markers for filtered places
                                overlays.clear()
                                filteredPlaces.forEach { place ->
                                    val marker = Marker(this).apply {
                                        position = GeoPoint(place.lat, place.lon)
                                        title = place.name
                                        snippet = buildString {
                                            append(place.amenity.replaceFirstChar { it.uppercase() })
                                            place.address?.let { append("\n$it") }
                                            place.phone?.let { append("\n📞 $it") }
                                            place.openingHours?.let { append("\n🕒 $it") }
                                        }
                                    }
                                    overlays.add(marker)
                                }
                                invalidate()
                            }
                        } catch (e: Exception) {
                            // Return a simple view if map creation fails
                            MapView(ctx)
                        }
                    },
                    update = { mapView ->
                        try {
                            mapView.controller.animateTo(GeoPoint(center.first, center.second))

                            mapView.overlays.clear()
                            filteredPlaces.forEach { place ->
                                val marker = Marker(mapView).apply {
                                    position = GeoPoint(place.lat, place.lon)
                                    title = place.name
                                    snippet = buildString {
                                        append(place.amenity.replaceFirstChar { it.uppercase() })
                                        place.address?.let { append("\n$it") }
                                        place.phone?.let { append("\n📞 $it") }
                                        place.openingHours?.let { append("\n🕒 $it") }
                                    }
                                }
                                mapView.overlays.add(marker)
                            }
                            mapView.invalidate()
                        } catch (e: Exception) {
                            // Handle map update errors silently
                        }
                    }
                )
            } else {
                // Fallback view when map is not initialized or has error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
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

                        Text(
                            text = if (mapError) "Map Error" else "Map Loading...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Center: ${String.format("%.4f", center.first)}, ${
                                String.format(
                                    "%.4f",
                                    center.second
                                )
                            }",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Showing ${filteredPlaces.size} markers",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
    var mapInitialized by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf(false) }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        try {
            Configuration.getInstance().apply {
                userAgentValue = "CareConnect/1.0"
                osmdroidTileCache = context.cacheDir
            }
            mapInitialized = true
        } catch (e: Exception) {
            mapError = true
        }
    }

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

        if (mapInitialized && !mapError) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    try {
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(center.first, center.second))

                            // Add markers
                            overlays.clear()
                            filteredPlaces.forEach { place ->
                                val marker = Marker(this).apply {
                                    position = GeoPoint(place.lat, place.lon)
                                    title = place.name
                                    snippet = buildString {
                                        append(place.amenity.replaceFirstChar { it.uppercase() })
                                        place.address?.let { append("\n$it") }
                                        place.phone?.let { append("\n📞 $it") }
                                        place.openingHours?.let { append("\n🕒 $it") }
                                    }
                                }
                                overlays.add(marker)
                            }
                            invalidate()
                        }
                    } catch (e: Exception) {
                        // Return a simple view if map creation fails
                        MapView(ctx)
                    }
                },
                update = { mapView ->
                    try {
                        mapView.controller.animateTo(GeoPoint(center.first, center.second))

                        mapView.overlays.clear()
                        filteredPlaces.forEach { place ->
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(place.lat, place.lon)
                                title = place.name
                                snippet = buildString {
                                    append(place.amenity.replaceFirstChar { it.uppercase() })
                                    place.address?.let { append("\n$it") }
                                    place.phone?.let { append("\n📞 $it") }
                                    place.openingHours?.let { append("\n🕒 $it") }
                                }
                            }
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                }
            )
        } else {
            // Fallback view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
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

                    Text(
                        text = if (mapError) "Map Error" else "Full Screen Map Loading...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Center: ${
                            String.format(
                                "%.4f",
                                center.first
                            )
                        }, ${String.format("%.4f", center.second)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (mapError) "Initialization failed" else "Would show ${filteredPlaces.size} markers",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
