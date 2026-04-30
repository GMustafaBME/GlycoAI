package com.sugarsaathi.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onComplete: (UserProfileData) -> Unit) {

    var currentScreen by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var diabetesType by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("en") }
    var selectedMeds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step $currentScreen of 3",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LinearProgressIndicator(
            progress = { currentScreen / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            color = TealGreen
        )

        when (currentScreen) {
            1 -> Screen1Language(
                selectedLanguage = language,
                onLanguageSelected = { language = it },
                onNext = { currentScreen = 2 }
            )
            2 -> Screen2BasicInfo(
                name = name,
                age = age,
                onNameChange = { name = it },
                onAgeChange = { age = it },
                onNext = { currentScreen = 3 },
                onBack = { currentScreen = 1 }
            )
            3 -> Screen3DiabetesType(
                selectedType = diabetesType,
                onTypeSelected = { diabetesType = it },
                onNext = { currentScreen = 4 },
                onBack = { currentScreen = 2 }
            )
            4 -> Screen4Medications(
                selectedMeds = selectedMeds,
                onMedToggle = { med ->
                    selectedMeds = if (selectedMeds.contains(med))
                        selectedMeds - med
                    else
                        selectedMeds + med
                },
                onBack = { currentScreen = 3 },
                onFinish = {
                    onComplete(
                        UserProfileData(
                            name = name.ifEmpty { "Friend" },
                            age = age.toIntOrNull() ?: 30,
                            diabetesType = diabetesType.ifEmpty { "unknown" },
                            language = language,
                            medications = selectedMeds.toList(),
                            onboardingDone = true
                        )
                    )
                }
            )
        }
    }
}

// ─── Screen 1: Language ───────────────────────────────

@Composable
fun Screen1Language(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("👋", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to GlycoAI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Your Daily Diabetes Companion",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Select your language",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))

        SelectableButton(
            text = "English",
            isSelected = selectedLanguage == "en",
            onClick = { onLanguageSelected("en") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SelectableButton(
            text = "اردو",
            isSelected = selectedLanguage == "ur",
            onClick = { onLanguageSelected("ur") }
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TealGreen)
        ) {
            Text("Next →", fontSize = 16.sp)
        }
    }
}

// ─── Screen 2: Basic Info ─────────────────────────────

@Composable
fun Screen2BasicInfo(
    name: String,
    age: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("👤", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tell us about yourself",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            placeholder = { Text("e.g. Ghulam Mustafa") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text("Your age") },
            placeholder = { Text("e.g. 45") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) { Text("← Back") }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = TealGreen),
                enabled = name.isNotEmpty()
            ) { Text("Next →") }
        }
    }
}

// ─── Screen 3: Diabetes Type ──────────────────────────

@Composable
fun Screen3DiabetesType(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val types = listOf(
        Triple("type1", "Type 1", "Insulin dependent"),
        Triple("type2", "Type 2", "Lifestyle related"),
        Triple("prediabetes", "Pre-diabetes", "At risk"),
        Triple("unknown", "Not sure", "I don't know my type")
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("🩺", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "What type of diabetes\ndo you have?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        types.forEach { (value, title, subtitle) ->
            SelectableCard(
                title = title,
                subtitle = subtitle,
                isSelected = selectedType == value,
                onClick = { onTypeSelected(value) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) { Text("← Back") }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = TealGreen),
                enabled = selectedType.isNotEmpty()
            ) { Text("Next →") }
        }
    }
}

// ─── Screen 4: Medications ────────────────────────────

@Composable
fun Screen4Medications(
    selectedMeds: Set<String>,
    onMedToggle: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val medications = listOf(
        "Glucophage", "Mixtard", "Amaryl",
        "Diamicron", "Lantus", "None"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("💊", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Current medications",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select all that apply",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        medications.forEach { med ->
            SelectableCard(
                title = med,
                subtitle = "",
                isSelected = selectedMeds.contains(med),
                onClick = { onMedToggle(med) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) { Text("← Back") }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = TealGreen)
            ) { Text("Start Chatting! 🚀") }
        }
    }
}

// ─── Reusable Components ──────────────────────────────

@Composable
fun SelectableButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Color(0xFFE1F5EE)
            else Color.Transparent
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) TealGreen else Color.Gray
        )
    ) {
        Text(
            text = text,
            color = if (isSelected) TealGreen else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold
            else FontWeight.Normal
        )
    }
}

@Composable
fun SelectableCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE1F5EE)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.5.dp,
            color = if (isSelected) TealGreen else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
            if (isSelected) {
                Text(
                    "✓",
                    color = TealGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}