package com.example.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.network.SessionManager

@Composable
fun SecureMailSetupScreen(
    sessionManager: SessionManager,
    onSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var apiKey by remember { mutableStateOf(sessionManager.secureMailApiKey ?: "") }
    var apiUrl by remember { mutableStateOf(sessionManager.secureMailApiUrl ?: "https://your-deployed-webmail-domain.com/api/v1/secure-message") }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Secure Mail Setup") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "API Endpoint & Key Generation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "We have integrated a secure backend API layer. It validates your incoming token and encrypts payload natively on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Enter your API key generated from the Integrations Dashboard to enable AES-GCM native encryption.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                label = { Text("Secure Mail API URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Secure Mail API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sessionManager.secureMailApiUrl = apiUrl
                    sessionManager.secureMailApiKey = apiKey
                    sessionManager.hasSetupSecureMail = true
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save & Continue")
            }

            TextButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                sessionManager.hasSetupSecureMail = true
                onSkip()
            }) {
                Text("Skip for now")
            }
        }
    }
}
