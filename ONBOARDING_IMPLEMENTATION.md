# Onboarding Implementation

## Overview

The CareConnect app now includes a comprehensive onboarding flow that collects user health
conditions and focus areas after signup. This ensures personalized experience for elderly care and
health management.

## Features Implemented

### 1. Onboarding Screen (`OnboardingScreen.kt`)

- **Health Conditions Selection**: Multi-choice grid layout with 20 options focused on elderly
  health
    - Includes conditions like Diabetes, High Blood Pressure, Arthritis, Memory Issues
    - Physical health and age-related conditions prioritized
    - "None" option available for users without specific conditions

- **Focus Areas Selection**: 20 wellness and care options
    - Physical Exercise, Nutrition & Diet, Mental Health, Social Connection
    - Elderly-specific areas like Fall Prevention, Memory Care, Medication Adherence
    - Emergency Preparedness and Family Communication included

- **UI Features**:
    - Collage-style FilterChip layout in 2-column grid
    - Responsive design with proper spacing
    - Loading states and error handling
    - Skip option for users who want to complete later

### 2. Onboarding ViewModel (`OnboardingViewModel.kt`)

- Handles onboarding completion process
- Saves selected data to Firestore with user document
- State management for loading, success, and error states
- Updates user document with onboarding completion flag

### 3. Updated Authentication Flow

- **FirebaseAuthViewModel** enhanced to check onboarding status
- New auth state: `NeedsOnboarding` for signed-up users who haven't completed onboarding
- Automatic onboarding check on login and signup
- Seamless transition from auth → onboarding → main app

### 4. MainActivity Integration

- Updated navigation flow to handle onboarding state
- Conditional rendering based on authentication and onboarding status
- Proper state transitions between screens

## Data Structure

User documents in Firestore now include:

```json
{
  "fullName": "User Name",
  "email": "user@example.com",
  "dateOfBirth": "01/01/1950",
  "gender": "Male",
  "healthConditions": ["Diabetes", "High Blood Pressure"],
  "focusAreas": ["Physical Exercise", "Medication Adherence"],
  "onboardingCompleted": true,
  "onboardingCompletedAt": 1234567890,
  "createdAt": 1234567890,
  "uid": "firebase_user_id"
}
```

## User Flow

1. **New User**: Sign Up → Onboarding → Home Screen
2. **Returning User (not onboarded)**: Login → Onboarding → Home Screen
3. **Returning User (onboarded)**: Login → Home Screen
4. **Skip Onboarding**: Users can skip and complete later

## Health Conditions Options

- Diabetes
- High Blood Pressure
- Heart Disease
- Arthritis
- Osteoporosis
- Depression
- Anxiety
- Memory Issues
- Joint Pain
- Back Pain
- Vision Problems
- Hearing Loss
- Sleep Disorders
- Chronic Fatigue
- Balance Issues
- Incontinence
- Medication Management
- Fall Risk
- Mobility Issues
- None

## Focus Areas Options

- Physical Exercise
- Nutrition & Diet
- Mental Health
- Social Connection
- Medication Adherence
- Fall Prevention
- Pain Management
- Memory Care
- Heart Health
- Bone Health
- Balance Training
- Flexibility
- Strength Building
- Stress Management
- Sleep Quality
- Vision Care
- Hearing Care
- Daily Activities
- Emergency Preparedness
- Family Communication

## Technical Implementation

- **UI Framework**: Jetpack Compose with Material 3
- **State Management**: ViewModel with StateFlow
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Layout**: LazyVerticalGrid for responsive chip layout
- **Navigation**: State-based conditional rendering

## Future Enhancements

- Add profile editing to modify onboarding selections
- Implement recommendation engine based on selected options
- Add progress tracking for focus areas
- Create personalized content based on health conditions