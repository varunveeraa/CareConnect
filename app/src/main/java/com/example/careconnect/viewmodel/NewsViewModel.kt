package com.example.careconnect.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.careconnect.api.NewsApiService
import com.example.careconnect.api.NewsArticle
import com.example.careconnect.api.NewsSource
import com.example.careconnect.database.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedArticle = MutableStateFlow<NewsArticle?>(null)
    val selectedArticle: StateFlow<NewsArticle?> = _selectedArticle.asStateFlow()

    private val newsApiService: NewsApiService

    // Using a demo API key - replace with your actual key from newsapi.org
    private val apiKey = "demo" // For testing, use actual key: https://newsapi.org/register

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        newsApiService = retrofit.create(NewsApiService::class.java)

        // Load sample articles on startup
        loadSampleArticles()
    }

    // Load sample articles for demonstration
    private fun loadSampleArticles() {
        _articles.value = getSampleArticlesList()
    }

    fun loadPersonalizedArticles(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Try to fetch real articles first, but keep sample articles as fallback
                val searchQueries = mapFocusAreasToQueries(user.focusArea)
                val allArticles = mutableListOf<NewsArticle>()

                // Try to fetch articles for each focus area
                var apiSuccess = false
                for (query in searchQueries.take(2)) { // Limit to 2 queries to avoid rate limits
                    try {
                        val response = newsApiService.getHealthArticles(
                            query = query,
                            apiKey = apiKey
                        )
                        if (response.articles.isNotEmpty()) {
                            allArticles.addAll(response.articles)
                            apiSuccess = true
                        }
                    } catch (e: Exception) {
                        // Continue with other queries if one fails
                    }
                }

                if (apiSuccess && allArticles.isNotEmpty()) {
                    // Use real articles if API worked
                    val uniqueArticles = allArticles
                        .distinctBy { it.url }
                        .take(10)
                    _articles.value = uniqueArticles
                } else {
                    // Fallback to personalized sample articles based on focus areas
                    _articles.value = getPersonalizedSampleArticles(user.focusArea)
                }

            } catch (e: Exception) {
                // Always fallback to sample articles
                _articles.value = getPersonalizedSampleArticles(user.focusArea)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGeneralHealthArticles() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = newsApiService.getTopHealthHeadlines(apiKey = apiKey)
                _articles.value = response.articles.take(10)
            } catch (e: Exception) {
                // Fallback to sample articles if API fails
                _articles.value = getSampleArticlesList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectArticle(article: NewsArticle?) {
        _selectedArticle.value = article
    }

    private fun mapFocusAreasToQueries(focusArea: String?): List<String> {
        if (focusArea.isNullOrEmpty()) return listOf("health wellness")

        // Parse focus areas (assuming it's a JSON string or comma-separated)
        val focusAreas = try {
            // If it's JSON array format, parse it properly
            focusArea.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
        } catch (e: Exception) {
            focusArea.split(",").map { it.trim() }
        }

        return focusAreas.map { area ->
            when (area.lowercase()) {
                "physical exercise" -> "fitness exercise workout"
                "nutrition & diet", "nutrition" -> "nutrition diet healthy eating"
                "mental health" -> "mental health depression anxiety"
                "social connection" -> "social health community wellness"
                "medication adherence" -> "medication management adherence"
                "fall prevention" -> "fall prevention elderly safety"
                "pain management" -> "pain management chronic pain"
                "memory care" -> "memory care alzheimer dementia"
                "heart health" -> "heart health cardiovascular"
                "bone health" -> "bone health osteoporosis calcium"
                "balance training" -> "balance training elderly fitness"
                "flexibility" -> "flexibility stretching mobility"
                "strength building" -> "strength training muscle building"
                "stress management" -> "stress management relaxation"
                "sleep quality" -> "sleep quality insomnia"
                "vision care" -> "vision care eye health"
                "hearing care" -> "hearing care audiology"
                "daily activities" -> "daily living activities elderly"
                "emergency preparedness" -> "emergency preparedness health safety"
                "family communication" -> "family health communication"
                else -> "$area health"
            }
        }.distinct()
    }

    private fun getPersonalizedSampleArticles(focusArea: String?): List<NewsArticle> {
        // Create personalized sample articles based on user's focus areas
        val allSampleArticles = getSampleArticlesList()

        if (focusArea.isNullOrEmpty()) {
            return allSampleArticles
        }

        // Filter articles based on focus areas
        val focusAreas = try {
            focusArea.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"").lowercase() }
        } catch (e: Exception) {
            focusArea.split(",").map { it.trim().lowercase() }
        }

        val relevantArticles = allSampleArticles.filter { article ->
            focusAreas.any { area ->
                when (area) {
                    "heart health", "physical exercise" -> article.title.contains(
                        "Heart",
                        ignoreCase = true
                    ) || article.title.contains("Exercise", ignoreCase = true)

                    "mental health" -> article.title.contains("Mental", ignoreCase = true)
                    "nutrition & diet", "nutrition" -> article.title.contains(
                        "Eating",
                        ignoreCase = true
                    ) || article.title.contains("Nutrition", ignoreCase = true)

                    "sleep quality" -> article.title.contains("Sleep", ignoreCase = true)
                    else -> true
                }
            }
        }

        return if (relevantArticles.isNotEmpty()) relevantArticles else allSampleArticles
    }

    private fun getSampleArticlesList(): List<NewsArticle> {
        return listOf(
            NewsArticle(
                source = NewsSource(id = "healthline", name = "Healthline"),
                author = "Health Team",
                title = "10 Tips for Better Heart Health",
                description = "Learn essential tips to keep your heart healthy and strong.",
                url = "https://healthline.com/heart-health",
                urlToImage = "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400&h=300&fit=crop",
                publishedAt = "2024-01-15T10:00:00Z",
                content = "Heart health is crucial for overall well-being. Regular exercise, a balanced diet, and proper sleep can significantly improve cardiovascular health. Studies show that 30 minutes of moderate exercise daily can reduce heart disease risk by up to 50%. Eating foods rich in omega-3 fatty acids, such as salmon and walnuts, helps maintain healthy cholesterol levels..."
            ),
            NewsArticle(
                source = NewsSource(id = "webmd", name = "WebMD"),
                author = "Medical Team",
                title = "Understanding Mental Health",
                description = "A comprehensive guide to mental wellness.",
                url = "https://webmd.com/mental-health",
                urlToImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=300&fit=crop",
                publishedAt = "2024-01-14T08:30:00Z",
                content = "Mental health is an essential part of overall health and well-being. It affects how we think, feel, and act. Good mental health helps us handle stress, relate to others, and make healthy choices. Common mental health conditions include anxiety, depression, and stress-related disorders. Seeking professional help when needed is important..."
            ),
            NewsArticle(
                source = NewsSource(id = "mayo", name = "Mayo Clinic"),
                author = "Mayo Team",
                title = "Exercise Benefits for Seniors",
                description = "How regular exercise improves quality of life.",
                url = "https://mayoclinic.org/exercise",
                urlToImage = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop",
                publishedAt = "2024-01-13T12:15:00Z",
                content = "Regular physical activity is one of the most important things seniors can do for their health. Exercise helps maintain bone density, muscle strength, and balance. It can also improve mood, cognitive function, and sleep quality. Even light activities like walking, swimming, or gardening can provide significant health benefits..."
            ),
            NewsArticle(
                source = NewsSource(id = "nutrition", name = "Nutrition Today"),
                author = "Diet Expert",
                title = "Healthy Eating After 50",
                description = "Nutritional guidelines for healthy aging.",
                url = "https://nutrition.com/healthy-eating",
                urlToImage = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&h=300&fit=crop",
                publishedAt = "2024-01-12T16:45:00Z",
                content = "Nutritional needs change as we age. After 50, it's important to focus on nutrient-dense foods that provide essential vitamins and minerals. Adequate protein intake helps maintain muscle mass, while calcium and vitamin D support bone health. Staying hydrated and limiting processed foods can help maintain energy levels..."
            ),
            NewsArticle(
                source = NewsSource(id = "sleep", name = "Sleep Foundation"),
                author = "Sleep Team",
                title = "Improving Sleep Quality",
                description = "Tips for better sleep as you age.",
                url = "https://sleepfoundation.org/quality",
                urlToImage = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=400&h=300&fit=crop",
                publishedAt = "2024-01-11T20:00:00Z",
                content = "Quality sleep becomes increasingly important with age. Many older adults experience changes in sleep patterns, including going to bed earlier and waking up earlier. Creating a consistent bedtime routine, maintaining a comfortable sleep environment, and avoiding caffeine late in the day can improve sleep quality..."
            ),
            NewsArticle(
                source = NewsSource(id = "diabetes", name = "Diabetes Care"),
                author = "Diabetes Team",
                title = "Managing Diabetes Daily",
                description = "Essential tips for diabetes management.",
                url = "https://diabetes.org/management",
                urlToImage = "https://images.unsplash.com/photo-1559757175-0eb30cd8c063?w=400&h=300&fit=crop",
                publishedAt = "2024-01-10T14:30:00Z",
                content = "Daily diabetes management involves monitoring blood glucose levels, taking medications as prescribed, and maintaining a healthy lifestyle. Regular blood sugar testing helps identify patterns and adjust treatment plans. A balanced diet, regular exercise, and stress management are key components of effective diabetes care..."
            ),
            NewsArticle(
                source = NewsSource(id = "arthritis", name = "Arthritis Foundation"),
                author = "Joint Health Team",
                title = "Joint Pain Relief Strategies",
                description = "Natural ways to reduce joint pain and stiffness.",
                url = "https://arthritis.org/pain-relief",
                urlToImage = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop",
                publishedAt = "2024-01-09T11:15:00Z",
                content = "Joint pain and stiffness can significantly impact daily activities. Low-impact exercises like swimming and yoga can help maintain joint flexibility and reduce pain. Heat and cold therapy, massage, and proper rest can also provide relief. Anti-inflammatory foods and maintaining a healthy weight help reduce joint stress..."
            )
        )
    }
}
