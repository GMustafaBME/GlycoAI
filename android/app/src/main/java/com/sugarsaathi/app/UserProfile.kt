package com.sugarsaathi.app

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Local storage file
val Context.dataStore by preferencesDataStore(name = "user_profile")

// Keys for storing each field
object ProfileKeys {
    val NAME = stringPreferencesKey("name")
    val AGE = intPreferencesKey("age")
    val DIABETES_TYPE = stringPreferencesKey("diabetes_type")
    val LANGUAGE = stringPreferencesKey("language")
    val MEDICATIONS = stringPreferencesKey("medications")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
}

// Profile data class
data class UserProfileData(
    val name: String = "",
    val age: Int = 0,
    val diabetesType: String = "unknown",
    val language: String = "en",
    val medications: List<String> = emptyList(),
    val onboardingDone: Boolean = false
)

// Handles saving and loading profile
class ProfileRepository(private val context: Context) {

    // Read profile
    val profileFlow: Flow<UserProfileData> = context.dataStore.data.map { prefs ->
        UserProfileData(
            name = prefs[ProfileKeys.NAME] ?: "",
            age = prefs[ProfileKeys.AGE] ?: 0,
            diabetesType = prefs[ProfileKeys.DIABETES_TYPE] ?: "unknown",
            language = prefs[ProfileKeys.LANGUAGE] ?: "en",
            medications = (prefs[ProfileKeys.MEDICATIONS] ?: "")
                .split(",")
                .filter { it.isNotEmpty() },
            onboardingDone = prefs[ProfileKeys.ONBOARDING_DONE] ?: false
        )
    }

    // Save profile
    suspend fun saveProfile(profile: UserProfileData) {
        context.dataStore.edit { prefs ->
            prefs[ProfileKeys.NAME] = profile.name
            prefs[ProfileKeys.AGE] = profile.age
            prefs[ProfileKeys.DIABETES_TYPE] = profile.diabetesType
            prefs[ProfileKeys.LANGUAGE] = profile.language
            prefs[ProfileKeys.MEDICATIONS] = profile.medications.joinToString(",")
            prefs[ProfileKeys.ONBOARDING_DONE] = true
        }
    }
}