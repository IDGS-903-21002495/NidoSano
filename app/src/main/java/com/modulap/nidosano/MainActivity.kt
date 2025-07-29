package com.modulap.nidosano

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.modulap.nidosano.service.MqttForegroundService
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.screens.*
import com.modulap.nidosano.ui.screens.auth.*
import com.modulap.nidosano.ui.screens.init.*
import com.modulap.nidosano.ui.screens.monitoring.*
import com.modulap.nidosano.ui.screens.notification.*
import com.modulap.nidosano.ui.screens.profile.*
import com.modulap.nidosano.ui.screens.tips.*
import com.modulap.nidosano.ui.theme.NidoSanoTheme
import com.modulap.nidosano.viewmodel.SecurityViewModel
import com.modulap.nidosano.viewmodel.SharedMqttViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startMqttService()

        val destinationFromNotification = intent.getStringExtra("notificationDestinationRoute")
        Log.d(TAG, "onCreate: destinationFromNotification = $destinationFromNotification")

        enableEdgeToEdge()

        //val currentUserId = Firebase.auth.currentUser?.uid

        setContent {
            NidoSanoTheme {
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.statusBarColor = android.graphics.Color.WHITE
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }

               // NidoSanoApp(startDestination = destinationFromNotification)
                NidoSanoAppWrapper(startDestination = destinationFromNotification)

            }
        }
    }

    private fun startMqttService() {
        val serviceIntent = Intent(this, MqttForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Log.d(TAG, "MqttForegroundService iniciado desde MainActivity.")
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NidoSanoApp(startDestination: String?, userId: String?) {
    val navController = rememberNavController()
    val sharedMqttViewModel: SharedMqttViewModel = viewModel()

    val defaultStart = Routes.Splash
    val actualStart = if (startDestination != null) {
        Log.d("NidoSanoApp", "Redirigiendo desde notificación a: $startDestination")
        when (startDestination) {
            "home" -> Routes.Home
            "feeding" -> Routes.Feeding
            "movement" -> Routes.Security
            else -> defaultStart
        }
    } else defaultStart

    LaunchedEffect(actualStart) {
        if (actualStart != defaultStart && navController.currentDestination?.route != actualStart) {
            navController.navigate(actualStart) {
                popUpTo(defaultStart) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = defaultStart) {
        composable(Routes.Splash) { SplashScreen(navController) }
        composable(Routes.Login) { LoginScreen(navController) }
        composable(Routes.Register) {
            CreateAccountScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.Feeding) { FeedingScreen(navController) }
        composable(Routes.FeedingSchedule) { ScheduleFeedingScreen(navController) }
        composable(Routes.FeedingScheduleList) { ScheduleListScreen(navController) }
        composable(Routes.Home) {
            MonitoringScreen(navController = navController, viewModel = sharedMqttViewModel)
        }
        composable(Routes.Security) {
            val securityViewModelFactory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
                        return SecurityViewModel(sharedMqttViewModel) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
            val securityViewModel: SecurityViewModel = viewModel(factory = securityViewModelFactory)
            SecurityScreen(navController = navController, viewModel = securityViewModel)
        }
        composable(Routes.Profile) {
            ProfileScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.EditProfile) {
            if (userId != null) {
                EditProfileScreen(
                    navController = navController,
                    userId = userId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.Password) { EditPasswordScreen(navController) }

        composable(Routes.Tips) { TipsScreen(navController) }
        composable(
            "tipDetail/{title}/{recomendation}/{measures}/{type}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("recomendation") { type = NavType.StringType },
                navArgument("measures") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val recomendation = backStackEntry.arguments?.getString("recomendation") ?: ""
            val measures = backStackEntry.arguments?.getString("measures") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: ""

            TipDetailScreen(title, recomendation, measures, type,  onBackClick = { navController.popBackStack() })
        }

        composable(Routes.Notification) {
            if (userId != null) {
                NotificationScreen(
                    navController = navController,
                    userId = userId,
                    coopId = "defaultChickenCoop"
                )
            }
        }
        composable(Routes.History) {
            HistorialScreen(
                navController = navController,
                userId = "MVGCTaZFfuL7XyePLKzu",
                coopId = "4MMnL8kHxbSV3ZplXThc",
                onViewMoreClick = { date ->
                    navController.navigate("history_detail_screen/$date")
                }
            )
        }
        composable(
            route = Routes.DetailWithArgs,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            HistorialDetailScreen(
                userId = "MVGCTaZFfuL7XyePLKzu",
                coopId = "4MMnL8kHxbSV3ZplXThc",
                date = date,
                navController = navController
            )
        }
    }
}