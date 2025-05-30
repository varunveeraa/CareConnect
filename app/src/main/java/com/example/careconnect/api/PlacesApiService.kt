package com.example.careconnect.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data classes for Overpass API
data class OverpassResponse(
    val version: Double,
    val generator: String,
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val type: String,
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>? = null
)

// Overpass API Service
interface OverpassApiService {
    @FormUrlEncoded
    @POST("interpreter")
    suspend fun queryNearbyPlaces(
        @Field("data") query: String
    ): Response<OverpassResponse>
}

// Nominatim API for geocoding
data class NominatimResponse(
    val place_id: Long,
    val licence: String,
    val osm_type: String,
    val osm_id: Long,
    val lat: String,
    val lon: String,
    val display_name: String,
    val address: NominatimAddress? = null
)

data class NominatimAddress(
    val suburb: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null
)

interface NominatimApiService {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 1,
        @Query("countrycodes") countryCodes: String = "au" // Australia only
    ): Response<List<NominatimResponse>>
}

// Data class to represent places from Overpass
data class OverpassPlace(
    val id: Long,
    val name: String,
    val amenity: String,
    val lat: Double,
    val lon: Double,
    val address: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val openingHours: String? = null,
    val tags: Map<String, String>? = null // Added to preserve original tags
) {
    companion object {
        fun fromOverpassElement(element: OverpassElement): OverpassPlace {
            val tags = element.tags ?: emptyMap()
            return OverpassPlace(
                id = element.id,
                name = tags["name"]
                    ?: "Unknown ${tags["amenity"]?.replaceFirstChar { it.uppercase() } ?: "Place"}",
                amenity = tags["amenity"] ?: tags["shop"] ?: tags["healthcare"] ?: "unknown",
                lat = element.lat,
                lon = element.lon,
                address = listOfNotNull(
                    tags["addr:street"],
                    tags["addr:housenumber"],
                    tags["addr:city"]
                ).joinToString(" ").takeIf { it.isNotBlank() },
                phone = tags["phone"],
                website = tags["website"],
                openingHours = tags["opening_hours"],
                tags = tags // Preserve original tags
            )
        }
    }
}
