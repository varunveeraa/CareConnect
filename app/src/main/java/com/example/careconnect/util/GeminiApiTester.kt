package com.example.careconnect.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiApiTester {
    
    suspend fun testApiConnection(): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyManager.GEMINI_API_KEY
        val testMessage = "Hello, can you respond with 'API connection successful'?"
        
        Log.d("GeminiApiTester", "Testing API connection...")
        Log.d("GeminiApiTester", "API Key: ${apiKey.take(10)}...${apiKey.takeLast(5)}")
        
        try {
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", testMessage)
                            })
                        })
                    })
                })
            }
            
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 30000
                readTimeout = 30000
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error message"
            }
            
            Log.d("GeminiApiTester", "Response Code: $responseCode")
            Log.d("GeminiApiTester", "Response Body: $responseBody")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonResponse = JSONObject(responseBody)
                if (jsonResponse.has("candidates")) {
                    val candidates = jsonResponse.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        if (candidate.has("content")) {
                            val content = candidate.getJSONObject("content")
                            if (content.has("parts")) {
                                val parts = content.getJSONArray("parts")
                                if (parts.length() > 0) {
                                    return@withContext parts.getJSONObject(0).getString("text")
                                }
                            }
                        }
                    }
                }
            }
            
            return@withContext "Test failed: $responseCode - $responseBody"
        } catch (e: Exception) {
            Log.e("GeminiApiTester", "Test error", e)
            return@withContext "Test error: ${e.message}"
        }
    }
}