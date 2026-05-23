package com.example.ui.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeTourScreen(
    onTryDemo: () -> Unit,
    onLoginClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val pages = listOf(
            WelcomePageData(
                title = "Welcome to Secure Mail",
                description = "Experience the next generation of JMAP powered email clients. Fast, reliable, and open.",
                icon = Icons.Default.Email
            ),
            WelcomePageData(
                title = "Unbreakable Encryption",
                description = "Our AES-GCM powered system guarantees that no one else can read your secure messages.",
                icon = Icons.Default.Lock
            ),
            WelcomePageData(
                title = "Beautifully Designed",
                description = "Crafted with Material 3, dynamic theming, and smooth animations.",
                icon = Icons.Default.PhoneAndroid
            )
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            WelcomePage(pageData = pages[page])
        }

        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage == 2) {
                Button(onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTryDemo() 
                }) {
                    Text("Try Demo")
                }
                FilledTonalButton(onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLoginClick() 
                }) {
                    Text("Log In")
                }
            } else {
                TextButton(onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLoginClick() 
                }) {
                    Text("Skip to Login")
                }
            }
        }
    }
}

data class WelcomePageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun WelcomePage(pageData: WelcomePageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = pageData.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = pageData.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = pageData.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
