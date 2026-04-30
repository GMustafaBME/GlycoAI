package com.sugarsaathi.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String = "",
    val messages: List<Message> = emptyList()
)

class ChatHistoryRepository(private val context: Context) {

    private val gson = Gson()
    private val historyDir get() = File(context.filesDir, "chat_history").also { it.mkdirs() }

    fun saveSession(messages: List<Message>) {
        if (messages.isEmpty()) return
        val title = messages.firstOrNull { it.role == "user" }?.content
            ?.take(50) ?: "Chat"
        val session = ChatSession(
            timestamp = System.currentTimeMillis(),
            title = title,
            messages = messages
        )
        val file = File(historyDir, "${session.id}.json")
        file.writeText(gson.toJson(session))
    }

    fun loadAllSessions(): List<ChatSession> {
        return historyDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull {
                try {
                    gson.fromJson(it.readText(), ChatSession::class.java)
                } catch (e: Exception) { null }
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    fun loadSession(id: String): ChatSession? {
        return try {
            val file = File(historyDir, "$id.json")
            if (file.exists()) gson.fromJson(file.readText(), ChatSession::class.java)
            else null
        } catch (e: Exception) { null }
    }

    fun deleteSession(id: String) {
        File(historyDir, "$id.json").delete()
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}