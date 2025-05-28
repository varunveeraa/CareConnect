package com.example.careconnect.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.careconnect.viewmodel.OnboardingViewModel
import com.example.careconnect.viewmodel.OnboardingState

@Composable
fun OnboardingScreen(
    onboardingViewModel: OnboardingViewModel = viewModel(),
    onOnboardingComplete: () -> Unit
) {
    var selectedHealthConditions by remember { mutableStateOf(setOf<String>()) }
    var selectedFocusAreas by remember { mutableStateOf(setOf<String>()) }
    
    val onboardingState by onboardingViewModel.onboardingState.collectAsState()
    
    // Handle onboarding completion
    LaunchedEffect(onboardingState) {
        if (onboardingState is OnboardingState.Success) {
            onOnboardingComplete()
        }
    }
    
    val healthConditions = listOf(
        "Diabetes", "High Blood Pressure", "Heart Disease", "Arthritis",
        "Osteoporosis", "Depression", "Anxiety", "Memory Issues",
        "Joint Pain", "Back Pain", "Vision Problems", "Hearing Loss",
        "Sleep Disorders", "Chronic Fatigue", "Balance Issues", "Incontinence",
        "Medication Management", "Fall Risk", "Mobility Issues", "None"
    )
    
    val focusAreas = listOf(
        "Physical Exercise", "Nutrition & Diet", "Mental Health", "Social Connection",
        "Medication Adherence", "Fall Prevention", "Pain Management", "Memory Care",
        "Heart Health", "Bone Health", "Balance Training", "Flexibility",
        "Strength Building", "Stress Management", "Sleep Quality", "Vision Care",
        "Hearing Care", "Daily Activities", "Emergency Preparedness", "Family Communication"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome to CareConnect!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Let's personalize your health journey",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Health Conditions Section
        Text(
            text = "What health conditions would you like support with?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Select all that apply",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(healthConditions) { condition ->
                val isSelected = selectedHealthConditions.contains(condition)
                
                FilterChip(
                    onClick = {
                        selectedHealthConditions = if (isSelected) {
                            selectedHealthConditions - condition
                        } else {
                            selectedHealthConditions + condition
                        }
                    },
                    label = { Text(condition, textAlign = TextAlign.Center) },
                    selected = isSelected,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Focus Areas Section
        Text(
            text = "What areas would you like to focus on?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Choose your health and wellness priorities",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(focusAreas) { area ->
                val isSelected = selectedFocusAreas.contains(area)
                
                FilterChip(
                    onClick = {
                        selectedFocusAreas = if (isSelected) {
                            selectedFocusAreas - area
                        } else {
                            selectedFocusAreas + area
                        }
                    },
                    label = { Text(area, textAlign = TextAlign.Center) },
                    selected = isSelected,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Error message
        val currentState = onboardingState
        if (currentState is OnboardingState.Error) {
            Text(
                text = currentState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Continue Button
        Button(
            onClick = {
                if (selectedHealthConditions.isNotEmpty() && selectedFocusAreas.isNotEmpty()) {
                    onboardingViewModel.completeOnboarding(
                        healthConditions = selectedHealthConditions.toList(),
                        focusAreas = selectedFocusAreas.toList()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedHealthConditions.isNotEmpty() && selectedFocusAreas.isNotEmpty() && onboardingState !is OnboardingState.Loading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (onboardingState is OnboardingState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Complete Setup",
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip button
        TextButton(
            onClick = {
                onboardingViewModel.completeOnboarding(
                    healthConditions = emptyList(),
                    focusAreas = emptyList()
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = onboardingState !is OnboardingState.Loading
        ) {
            Text("Skip for now")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
