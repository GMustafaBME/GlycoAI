package com.sugarsaathi.app

// What we send TO the server
data class ProfileData(
    val name: String,
    val age: Int,
    val diabetes_type: String,
    val hba1c: Float? = null,
    val medications: List<String> = emptyList(),
    val complications: List<String> = emptyList(),
    val fasts_ramadan: String = "no",
    val language: String = "en",
    val response_style: String = "simple"
)

data class ChatRequest(
    val message: String,
    val profile: ProfileData,
    val conversation_history: List<Map<String, String>>,
    val image_data: String? = null,
    val image_type: String? = null,
    val document_data: String? = null,
    val document_type: String? = null,
    val document_name: String? = null
)

// What we receive FROM the server
data class ChatResponse(
    val response: String,
    val safety_triggered: Boolean
)

// A single chat message (user or AI)
data class Message(
    val role: String,
    val content: String
)