package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.network.SessionManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(sessionManager: SessionManager) {
    val haptics = LocalHapticFeedback.current
    var secureApiUrl by remember { mutableStateOf(sessionManager.secureMailApiUrl ?: "") }
    var secureApiKey by remember { mutableStateOf(sessionManager.secureMailApiKey ?: "") }
    
    val themeMode by sessionManager.themeMode.collectAsState()
    var selectedLanguage by remember { mutableStateOf(sessionManager.languageCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = themeMode == 0,
                onClick = { sessionManager.setThemeMode(0); haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                label = { Text("System") }
            )
            FilterChip(
                selected = themeMode == 1,
                onClick = { sessionManager.setThemeMode(1); haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                label = { Text("Light") }
            )
            FilterChip(
                selected = themeMode == 2,
                onClick = { sessionManager.setThemeMode(2); haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                label = { Text("Dark") }
            )
        }

        HorizontalDivider()
        
        Text("Language", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedLanguage == "en",
                onClick = { 
                    selectedLanguage = "en"
                    sessionManager.languageCode = "en"
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress) 
                },
                label = { Text("EN") }
            )
            FilterChip(
                selected = selectedLanguage == "es",
                onClick = { 
                    selectedLanguage = "es"
                    sessionManager.languageCode = "es"
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress) 
                },
                label = { Text("ES") }
            )
        }

        HorizontalDivider()

        Text("Secure Mail API Integration", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = secureApiUrl,
            onValueChange = { secureApiUrl = it },
            label = { Text("API URL") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = secureApiKey,
            onValueChange = { secureApiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            sessionManager.secureMailApiUrl = secureApiUrl
            sessionManager.secureMailApiKey = secureApiKey
        }) {
            Text("Save API Settings")
        }
    }
}
