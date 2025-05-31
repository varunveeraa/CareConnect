package com.example.careconnect.api

import retrofit2.http.GET
import retrofit2.http.Query

// API Data Models
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticle>
)

data class NewsArticle(
    val source: NewsSource,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class NewsSource(
    val id: String?,
    val name: String
)

// API Service Interface
interface NewsApiService {
    @GET("v2/everything")
    suspend fun getHealthArticles(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "popularity",
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse

    @GET("v2/top-headlines")
    suspend fun getTopHealthHeadlines(
        @Query("category") category: String = "health",
        @Query("apiKey") apiKey: String,
        @Query("country") country: String = "us",
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse
}
