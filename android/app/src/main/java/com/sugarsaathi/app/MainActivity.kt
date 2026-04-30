package com.sugarsaathi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sugarsaathi.app.ui.theme.SugarSaathiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileRepo = ProfileRepository(this)

        setContent {
            SugarSaathiTheme {

                var profile by remember { mutableStateOf<UserProfileData?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var showSplash by remember { mutableStateOf(true) }
                var showOnboarding by remember { mutableStateOf(false) }
                var showHistory by remember { mutableStateOf(false) }
                var selectedSession by remember { mutableStateOf<ChatSession?>(null) }
                val chatViewModel: ChatViewModel = viewModel()

                LaunchedEffect(Unit) {
                    val savedProfile = profileRepo.profileFlow.first()
                    profile = savedProfile
                    isLoading = false
                }

                when {
                    showSplash -> {
                        SplashScreen(onFinished = { showSplash = false })
                    }

                    isLoading -> LoadingScreen()

                    profile?.onboardingDone != true || showOnboarding -> {
                        OnboardingScreen(
                            onComplete = { newProfile ->
                                lifecycleScope.launch {
                                    profileRepo.saveProfile(newProfile)
                                    profile = newProfile
                                    showOnboarding = false
                                }
                            }
                        )
                    }

                    selectedSession != null -> {
                        ChatDetailScreen(
                            session = selectedSession!!,
                            onBack = { selectedSession = null }
                        )
                    }

                    showHistory -> {
                        ChatHistoryScreen(
                            onBack = { showHistory = false },
                            onOpenSession = { session ->
                                selectedSession = session
                            }
                        )
                    }

                    else -> {
                        WelcomeScreen(
                            profile = profile!!,
                            onChangeInfo = { showOnboarding = true },
                            onHistoryClick = { showHistory = true },
                            chatViewModel = chatViewModel
                        )
                    }
                }
            }
        }
    }
}