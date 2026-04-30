package com.sugarsaathi.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.util.Locale

val TealGreen = Color(0xFF1D9E75)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userProfile: UserProfileData,
    onHistoryClick: () -> Unit = {},
    chatViewModel: ChatViewModel = viewModel()
) {
    val uiState by chatViewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val isUrdu = userProfile.language == "ur"
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    var selectedImageMime by remember { mutableStateOf<String?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDocName by remember { mutableStateOf<String?>(null) }
    var selectedDocBase64 by remember { mutableStateOf<String?>(null) }
    var selectedDocMime by remember { mutableStateOf<String?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        chatViewModel.initHistory(context)
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                @Suppress("DEPRECATION")
                tts?.language = if (isUrdu) Locale("ur") else Locale.ENGLISH
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isNotEmpty() || selectedImageBase64 != null || selectedDocBase64 != null) {
            val messageText = text.ifEmpty {
                if (selectedDocBase64 != null) "Please analyze this document."
                else "Please analyze this image."
            }
            inputText = ""
            chatViewModel.sendMessage(
                userText = messageText,
                profile = userProfile,
                imageBase64 = selectedImageBase64,
                imageMimeType = selectedImageMime,
                documentBase64 = selectedDocBase64,
                documentMimeType = selectedDocMime,
                documentName = selectedDocName
            )
            selectedImageUri = null
            selectedImageBase64 = null
            selectedImageMime = null
            selectedDocName = null
            selectedDocBase64 = null
            selectedDocMime = null
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: ""
            if (spokenText.isNotEmpty()) {
                chatViewModel.sendMessage(spokenText, userProfile)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                selectedDocName = null
                selectedDocBase64 = null
                selectedDocMime = null
                selectedImageUri = uri
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    selectedImageBase64 = bytes?.let { b -> Base64.encodeToString(b, Base64.NO_WRAP) }
                    selectedImageMime = "image/jpeg"
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedDocName = null
            selectedDocBase64 = null
            selectedDocMime = null
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                selectedImageBase64 = bytes?.let { b -> Base64.encodeToString(b, Base64.NO_WRAP) }
                selectedImageMime = context.contentResolver.getType(it) ?: "image/jpeg"
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = OpenDocument()
    ) { uri ->
        uri?.let {
            selectedImageUri = null
            selectedImageBase64 = null
            selectedImageMime = null
            try {
                val mimeType = context.contentResolver.getType(it) ?: "application/pdf"
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                selectedDocBase64 = bytes?.let { b -> Base64.encodeToString(b, Base64.NO_WRAP) }
                selectedDocMime = mimeType
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst() && nameIndex >= 0) {
                        selectedDocName = c.getString(nameIndex)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isUrdu) "ur-PK" else "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (isUrdu) "بولیں..." else "Speak now...")
        }
        speechLauncher.launch(intent)
    }

    fun openCamera() {
        try {
            val photoFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) { e.printStackTrace() }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (isUrdu) "گلائیکو اے آئی" else "GlycoAI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isUrdu) "آپ کا روزانہ ذیابیطس ساتھی" else "Your Daily Diabetes Companion",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            },
            actions = {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TealGreen,
                titleContentColor = Color.White
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item { WelcomeMessage(isUrdu = isUrdu, name = userProfile.name) }
            }
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    onSpeak = { text ->
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                )
            }
            if (uiState.isLoading) {
                item { TypingIndicator(isUrdu = isUrdu) }
            }
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Image preview
        selectedImageUri?.let { uri ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = {
                        selectedImageUri = null
                        selectedImageBase64 = null
                        selectedImageMime = null
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color.White
                    )
                }
            }
        }

        // Document preview
        selectedDocName?.let { name ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Document",
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF3F51B5)
                    )
                    IconButton(
                        onClick = {
                            selectedDocName = null
                            selectedDocBase64 = null
                            selectedDocMime = null
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove document",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Speed dial menu
        if (expanded) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            ) {
                SpeedDialItem(
                    label = if (isUrdu) "آواز" else "Voice",
                    color = Color(0xFF1A6B8A),
                    icon = Icons.Default.Mic,
                    onClick = { expanded = false; startVoiceInput() }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SpeedDialItem(
                    label = if (isUrdu) "گیلری" else "Gallery",
                    color = Color(0xFF7C5CBF),
                    icon = Icons.Default.Image,
                    onClick = {
                        expanded = false
                        imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SpeedDialItem(
                    label = if (isUrdu) "کیمرہ" else "Camera",
                    color = Color(0xFF0D7A5F),
                    icon = Icons.Default.CameraAlt,
                    onClick = { expanded = false; openCamera() }
                )
                Spacer(modifier = Modifier.height(6.dp))
                SpeedDialItem(
                    label = if (isUrdu) "دستاویز" else "Document",
                    color = Color(0xFF3F51B5),
                    icon = Icons.Default.Description,
                    onClick = {
                        expanded = false
                        documentPicker.launch(arrayOf("application/pdf", "text/plain"))
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = if (expanded) Color.Gray else Color(0xFF555555),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "More",
                    tint = Color.White
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(if (isUrdu) "اپنا سوال لکھیں..." else "Type your question...")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() })
            )

            FloatingActionButton(
                onClick = { sendMessage() },
                containerColor = TealGreen,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SpeedDialItem(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
    }
}

@Composable
fun WelcomeMessage(isUrdu: Boolean, name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5EE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isUrdu) "السلام علیکم! 👋" else "Hello, $name! 👋",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isUrdu)
                    "میں گلائیکو اے آئی ہوں۔ ذیابیطس کے بارے میں کچھ بھی پوچھیں!"
                else
                    "I am GlycoAI. Ask me anything about diabetes!",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message, onSpeak: ((String) -> Unit)? = null) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (isUser) TealGreen
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.content
                        .replace("**", "")
                        .replace("##", "")
                        .replace("---", "─────"),
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            if (!isUser && onSpeak != null) {
                IconButton(
                    onClick = { onSpeak(message.content) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = TealGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(isUrdu: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = TealGreen
        )
        Text(
            text = if (isUrdu) "جواب آ رہا ہے..." else "Thinking...",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}