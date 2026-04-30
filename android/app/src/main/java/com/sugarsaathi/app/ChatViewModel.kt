package com.sugarsaathi.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private var historyRepo: ChatHistoryRepository? = null

    fun initHistory(context: Context) {
        historyRepo = ChatHistoryRepository(context)
    }

    fun saveCurrentSession() {
        val messages = _uiState.value.messages
        if (messages.isNotEmpty()) {
            historyRepo?.saveSession(messages)
        }
    }

    fun sendMessage(
        userText: String,
        profile: UserProfileData,
        imageBase64: String? = null,
        imageMimeType: String? = null,
        documentBase64: String? = null,
        documentMimeType: String? = null,
        documentName: String? = null
    ) {
        if (userText.isBlank() && imageBase64 == null && documentBase64 == null) return

        val userMessage = Message(role = "user", content = userText)
        val currentMessages = _uiState.value.messages + userMessage

        _uiState.value = _uiState.value.copy(
            messages = currentMessages,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val history = currentMessages.dropLast(1).map {
                    mapOf("role" to it.role, "content" to it.content)
                }

                val profileData = ProfileData(
                    name = profile.name,
                    age = profile.age,
                    diabetes_type = profile.diabetesType,
                    hba1c = null,
                    medications = profile.medications,
                    complications = emptyList(),
                    language = profile.language,
                    response_style = "simple"
                )

                val request = ChatRequest(
                    message = userText,
                    profile = profileData,
                    conversation_history = history,
                    image_data = imageBase64,
                    image_type = imageMimeType,
                    document_data = documentBase64,
                    document_type = documentMimeType,
                    document_name = documentName
                )

                val response = NetworkModule.apiService.sendMessage(request)

                val aiMessage = Message(
                    role = "assistant",
                    content = response.response
                )

                val updatedMessages = currentMessages + aiMessage

                _uiState.value = _uiState.value.copy(
                    messages = updatedMessages,
                    isLoading = false
                )

                // Auto-save after every AI response
                historyRepo?.saveSession(updatedMessages)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Connection error: ${e.message}"
                )
            }
        }
    }
}