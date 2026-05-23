package com.example.ui.email

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailScreen(
    viewModel: EmailDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (uiState.email != null) {
                val email = uiState.email!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = email.subject,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "From: ",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${email.from?.name} <${email.from?.email}>",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (email.to.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "To: ",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = email.to.joinToString { "${it.name} <${it.email}>" },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                    }

                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = false
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = false
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                webViewClient = WebViewClient()
                                
                                val rawHtml = email.body
                                val formattedHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                  body { font-family: sans-serif; line-height: 1.5; padding: 8px; color: #333333; word-wrap: break-word; }
                                  img { max-width: 100%; height: auto; }
                                  @media (prefers-color-scheme: dark) {
                                     body { color: #DDDDDD; background-color: #121212; }
                                     a { color: #8AB4F8; }
                                  }
                                </style>
                                </head>
                                <body>
                                $rawHtml
                                </body>
                                </html>
                                """.trimIndent()

                                loadDataWithBaseURL(null, formattedHtml, "text/html", "UTF-8", null)
                            }
                        }
                    )
                }
            }
        }
    }
}
