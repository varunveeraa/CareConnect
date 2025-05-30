package com.example.careconnect.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.careconnect.api.NewsArticle
import com.example.careconnect.database.User
import com.example.careconnect.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User? = null,
    newsViewModel: NewsViewModel = viewModel(),
    onArticleClick: ((NewsArticle) -> Unit)? = null
) {
    val articles by newsViewModel.articles.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Load articles based on user's focus areas
            newsViewModel.loadPersonalizedArticles(user)
        } ?: run {
            // Load general health articles for users without profiles
            newsViewModel.loadGeneralHealthArticles()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Single row with all four items in one box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricCard(
                value = "1000",
                label = "Steps",
                icon = Icons.Default.DirectionsRun,
                iconColor = Color.Blue
            )
            MetricCard(
                value = "70",
                label = "Heart Rate",
                icon = Icons.Default.Favorite,
                iconColor = Color.Red
            )
            MetricCard(
                value = "7",
                label = "Sleep",
                icon = Icons.Default.Nightlight,
                iconColor = Color.Gray
            )
            MetricCard(
                value = "250",
                label = "Calories",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = Color(0xFF9C27B0) // Purple color
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reminders Section (moved above articles)
        Text(
            text = "Reminders",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "“Hi mom, did you take your iron tablet?”")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Send logic */ }) {
                    Text("Send")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Articles Section
        Text(
            text = "Health Articles for You",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Personalized content based on your health focus",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (articles.isEmpty()) {
            NoArticlesMessage()
        } else {
            ArticlesCarousel(
                articles = articles,
                onArticleClick = onArticleClick
            )
        }
    }
}

@Composable
fun ArticlesCarousel(
    articles: List<NewsArticle>,
    onArticleClick: ((NewsArticle) -> Unit)? = null
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles) { article ->
            ArticleCard(
                article = article,
                onArticleClick = onArticleClick
            )
        }
    }
}

@Composable
fun ArticleCard(
    article: NewsArticle,
    onArticleClick: ((NewsArticle) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp)
            .clickable { onArticleClick?.invoke(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Article Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.urlToImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Article image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Article Title
            Text(
                text = article.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1A1A1A),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Source and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = formatDate(article.publishedAt),
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NoArticlesMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Article,
                contentDescription = "No Articles",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No articles available",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            
            Text(
                text = "Complete your health profile to see personalized articles",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Icon at the top
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Label text
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A) // Same dark color as the value text
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Value text
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A) // Very dark for emphasis
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        "Recent"
    }
}
