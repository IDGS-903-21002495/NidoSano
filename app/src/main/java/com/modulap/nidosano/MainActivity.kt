package com.modulap.nidosano

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi // Asegúrate de tener esta importación
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

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    // La anotación en MainActivity también debe ser corregida si la tienes
    // @RequiresApi(Build.VERSION_CODES.N) // Ejemplo de corrección si la tenías aquí
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar el MqttForegroundService al crear la actividad
        startMqttService()

        val destinationFromNotification = intent.getStringExtra("notificationDestinationRoute")
        Log.d(TAG, "onCreate: destinationFromNotification = $destinationFromNotification")

        enableEdgeToEdge()

        setContent {
            NidoSanoTheme {
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.statusBarColor = android.graphics.Color.WHITE
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }

                // Aquí no se necesita la anotación @RequiresApi
                NidoSanoApp(startDestination = destinationFromNotification)
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

// **CORRECCIÓN AQUÍ:**
@RequiresApi(Build.VERSION_CODES.N) // Cambiado de Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
@Composable
fun NidoSanoApp(startDestination: String?) {
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
        composable(Routes.Register) { CreateAccountScreen(navController) }
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
            ProfileScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable(Routes.EditProfile) {
            EditProfileScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable(Routes.Tips) { TipsScreen(navController) }
        composable(Routes.Notification) { NotificationScreen(navController) }
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