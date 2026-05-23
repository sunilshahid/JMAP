package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.data.EmailRepository
import com.example.data.MailboxRepository
import com.example.network.JmapClient
import com.example.network.JmapWebSocketManager
import com.example.network.SessionManager
import com.example.ui.compose.ComposeEmailScreen
import com.example.ui.compose.ComposeViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.email.EmailDetailScreen
import com.example.ui.email.EmailDetailViewModel
import com.example.ui.login.LoginScreen
import com.example.ui.login.LoginViewModel
import com.example.ui.theme.JmapClientTheme

import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.CalendarRepository
import com.example.data.ContactRepository
import com.example.network.NotificationHelper

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

class MainActivity : ComponentActivity() {

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    setContent {
      JmapClientTheme {
        val navController = rememberNavController()
        val sessionManager = remember { SessionManager(this) }
        val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
        
        val jmapClient = remember { JmapClient(sessionManager) }
        val mailboxRepository = remember { MailboxRepository(jmapClient, sessionManager) }
        val emailRepository = remember { EmailRepository(jmapClient, sessionManager) }
        val contactRepository = remember { ContactRepository(jmapClient, sessionManager) }
        val calendarRepository = remember { CalendarRepository(jmapClient, sessionManager) }
        val webSocketManager = remember { JmapWebSocketManager(sessionManager) }
        val notificationHelper = remember { NotificationHelper(this) }

        var dashboardViewModelKey by remember { mutableStateOf(0) }

        val startDest = remember(isLoggedIn, sessionManager.hasSeenWelcomeTour, sessionManager.hasSetupSecureMail) {
            when {
                !sessionManager.hasSeenWelcomeTour -> "welcome_tour"
                !isLoggedIn -> "login"
                !sessionManager.hasSetupSecureMail && !sessionManager.isDemoMode -> "secure_mail_setup"
                else -> "dashboard"
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val haptics = LocalHapticFeedback.current
          NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) },
            popEnterTransition = { fadeIn(animationSpec = tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) },
            popExitTransition = { fadeOut(animationSpec = tween(500)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) }
          ) {
            composable("welcome_tour") {
                com.example.ui.welcome.WelcomeTourScreen(
                    onTryDemo = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        sessionManager.hasSeenWelcomeTour = true
                        sessionManager.setDemoMode(true)
                        navController.navigate("dashboard") {
                            popUpTo("welcome_tour") { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        sessionManager.hasSeenWelcomeTour = true
                        navController.navigate("login") {
                            popUpTo("welcome_tour") { inclusive = true }
                        }
                    }
                )
            }
            composable("login") {
              val loginViewModel = remember { LoginViewModel(sessionManager) }
              LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                  navController.navigate("secure_mail_setup") {
                    popUpTo("login") { inclusive = true }
                  }
                }
              )
            }
            composable("secure_mail_setup") {
                com.example.ui.login.SecureMailSetupScreen(
                    sessionManager = sessionManager,
                    onSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("secure_mail_setup") { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate("dashboard") {
                            popUpTo("secure_mail_setup") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
              val dashboardViewModel = remember(dashboardViewModelKey) { 
                  DashboardViewModel(mailboxRepository, emailRepository, contactRepository, calendarRepository, sessionManager, webSocketManager, notificationHelper) 
              }
              DashboardScreen(
                viewModel = dashboardViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onEmailClick = { emailId ->
                    navController.navigate("email/$emailId")
                },
                onComposeClick = {
                    navController.navigate("compose")
                }
              )
            }
            composable(
                route = "email/{emailId}",
                arguments = listOf(navArgument("emailId") { type = NavType.StringType })
            ) { backStackEntry ->
                val emailId = backStackEntry.arguments?.getString("emailId") ?: return@composable
                val emailDetailViewModel = remember(emailId) { EmailDetailViewModel(emailId, emailRepository) }
                EmailDetailScreen(
                    viewModel = emailDetailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("compose") {
                val composeViewModel = remember { ComposeViewModel(emailRepository, mailboxRepository) }
                ComposeEmailScreen(
                    viewModel = composeViewModel,
                    onClose = { navController.popBackStack() }
                )
            }
          }
        }
      }
    }
  }
}

