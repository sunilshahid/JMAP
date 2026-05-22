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

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "dashboard" else "login",
            modifier = Modifier.padding(innerPadding)
          ) {
            composable("login") {
              val loginViewModel = remember { LoginViewModel(sessionManager) }
              LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                  navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
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

