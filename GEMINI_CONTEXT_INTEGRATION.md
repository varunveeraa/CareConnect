# Gemini API Context Integration

This implementation provides personalized AI conversations by automatically including user context (
personal information, health metrics, location, etc.) in Gemini API requests.

## Features

### User Context Includes:

- **User Information**: Name, age, gender, health conditions, focus areas
- **Current Health Metrics**: Today's steps, heart rate, sleep hours, calories
- **Location Data**: GPS coordinates (if permission granted)
- **Temporal Context**: Current date and time

### How It Works

1. **Context Building** (`GeminiContextBuilder`):
    - Collects user data from Firestore
    - Fetches current health metrics from HealthDataManager
    - Gets location from LocationManager
    - Formats everything into a comprehensive context message

2. **Enhanced Chat Repository** (`ChatRepository`):
    - Before sending user messages to Gemini API
    - Automatically prepends context information
    - Provides personalized, health-aware responses

3. **Context-Aware ViewModels**:
    - `ChatViewModel` now accepts Context parameter
    - Uses `ChatViewModelFactory` for dependency injection
    - Seamlessly integrates with existing UI

## Example Context Message Format

```
Context about the user you're chatting with:

User Information:
- Name: John Doe
- Age: 32 years old
- Gender: Male
- Health Conditions: Diabetes, Hypertension
- Health Focus Areas: Weight Loss, Heart Health

Current Information:
- Date: 2024-01-15
- Time: 14:30
- GPS Coordinates: 40.7128, -74.0060
- Location Accuracy: 10m

Today's Health Metrics:
- Steps: 8,500
- Heart Rate: 72.0 bpm
- Sleep: 7.2 hours
- Calories: 2,100
- Last Updated: 14:25

Please use this context to provide personalized and relevant health advice and responses. Take into account the user's health conditions, current metrics, and focus areas when responding. Be supportive and provide actionable advice when appropriate.

User's message: How am I doing with my fitness goals today?
```

## Usage

The system automatically works with existing chat functionality. When users send messages:

1. Context is built automatically
2. User message is appended to context
3. Complete contextualized message is sent to Gemini
4. AI responds with personalized, health-aware advice

## Privacy & Permissions

- Location context requires location permissions
- Health data is only included if user has connected a wearable device
- All context building gracefully handles missing data
- No user data is stored in API calls - only used for context

## Implementation Details

### Key Classes:

- `GeminiContextBuilder`: Collects and formats user context
- `ChatRepository`: Enhanced with context integration
- `ChatViewModel` & `ChatViewModelFactory`: Context-aware chat management

### Dependencies:

- Firebase Auth (user identification)
- Firebase Firestore (user data)
- HealthDataManager (health metrics)
- LocationManager (GPS coordinates)

This provides a seamless, personalized AI health assistant experience while maintaining user privacy
and data security.