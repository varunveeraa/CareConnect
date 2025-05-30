package com.example.careconnect.repository

import com.example.careconnect.api.NominatimApiService
import com.example.careconnect.api.OverpassApiService
import com.example.careconnect.api.OverpassPlace
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OverpassRepository {
    private val apiService: OverpassApiService
    private val nominatimService: NominatimApiService

    init {
        val overpassRetrofit = Retrofit.Builder()
            .baseUrl("https://overpass-api.de/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val nominatimRetrofit = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = overpassRetrofit.create(OverpassApiService::class.java)
        nominatimService = nominatimRetrofit.create(NominatimApiService::class.java)
    }

    // Geocoding function to convert suburb names to coordinates
    suspend fun geocodeSuburb(suburbName: String): Pair<Double, Double>? {
        return try {
            val query = "$suburbName, Melbourne, Victoria, Australia"
            val response = nominatimService.searchLocation(query)

            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                val result = response.body()!!.first()
                Pair(result.lat.toDouble(), result.lon.toDouble())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getNearbyPharmacies(
        latitude: Double,
        longitude: Double,
        radius: Int = 3000 // Increased for Melbourne's spread out suburbs
    ): List<OverpassPlace> {
        return try {
            val query = """
                [out:json];
                (
                  node["amenity"="pharmacy"](around:$radius,$latitude,$longitude);
                  node["shop"="chemist"](around:$radius,$latitude,$longitude);
                );
                out;
            """.trimIndent()

            val response = apiService.queryNearbyPlaces(query)

            if (response.isSuccessful) {
                response.body()?.elements?.map { element ->
                    OverpassPlace.fromOverpassElement(element)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNearbyClinics(
        latitude: Double,
        longitude: Double,
        radius: Int = 5000 // Larger radius for Melbourne medical facilities
    ): List<OverpassPlace> {
        return try {
            val query = """
                [out:json];
                (
                  node["amenity"="clinic"](around:$radius,$latitude,$longitude);
                  node["amenity"="hospital"](around:$radius,$latitude,$longitude);
                  node["amenity"="doctors"](around:$radius,$latitude,$longitude);
                  node["healthcare"="clinic"](around:$radius,$latitude,$longitude);
                  node["healthcare"="hospital"](around:$radius,$latitude,$longitude);
                  node["healthcare"="doctor"](around:$radius,$latitude,$longitude);
                  node["healthcare"="centre"](around:$radius,$latitude,$longitude);
                  node["amenity"="dentist"](around:$radius,$latitude,$longitude);
                  node["healthcare"="dentist"](around:$radius,$latitude,$longitude);
                );
                out;
            """.trimIndent()

            val response = apiService.queryNearbyPlaces(query)

            if (response.isSuccessful) {
                response.body()?.elements?.map { element ->
                    OverpassPlace.fromOverpassElement(element)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllNearbyHealthcare(
        latitude: Double,
        longitude: Double,
        radius: Int = 5000 // Larger radius for Melbourne's layout
    ): List<OverpassPlace> {
        return try {
            val query = """
                [out:json];
                (
                  node["amenity"="pharmacy"](around:$radius,$latitude,$longitude);
                  node["shop"="chemist"](around:$radius,$latitude,$longitude);
                  node["amenity"="clinic"](around:$radius,$latitude,$longitude);
                  node["amenity"="hospital"](around:$radius,$latitude,$longitude);
                  node["amenity"="doctors"](around:$radius,$latitude,$longitude);
                  node["healthcare"="clinic"](around:$radius,$latitude,$longitude);
                  node["healthcare"="hospital"](around:$radius,$latitude,$longitude);
                  node["healthcare"="doctor"](around:$radius,$latitude,$longitude);
                  node["healthcare"="centre"](around:$radius,$latitude,$longitude);
                  node["amenity"="dentist"](around:$radius,$latitude,$longitude);
                  node["healthcare"="dentist"](around:$radius,$latitude,$longitude);
                );
                out;
            """.trimIndent()

            val response = apiService.queryNearbyPlaces(query)

            if (response.isSuccessful) {
                response.body()?.elements?.map { element ->
                    OverpassPlace.fromOverpassElement(element)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Melbourne-specific quick search functions
    suspend fun getMelbourneCBDHealthcare(): List<OverpassPlace> {
        // Melbourne CBD coordinates: -37.8136, 144.9631
        return getAllNearbyHealthcare(-37.8136, 144.9631, 2000)
    }

    suspend fun getSouthYarraHealthcare(): List<OverpassPlace> {
        // South Yarra coordinates: -37.8394, 144.9926
        return getAllNearbyHealthcare(-37.8394, 144.9926, 3000)
    }

    suspend fun getStKildaHealthcare(): List<OverpassPlace> {
        // St Kilda coordinates: -37.8676, 144.9803
        return getAllNearbyHealthcare(-37.8676, 144.9803, 3000)
    }

    suspend fun getRichmondHealthcare(): List<OverpassPlace> {
        // Richmond coordinates: -37.8197, 144.9917
        return getAllNearbyHealthcare(-37.8197, 144.9917, 3000)
    }
}
