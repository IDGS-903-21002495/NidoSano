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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.modulap.nidosano.ui.viewmodel.AuthViewModel
import com.modulap.nidosano.viewmodel.SecurityViewModel
import com.modulap.nidosano.viewmodel.SharedMqttViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// **NUEVAS IMPORTACIONES NECESARIAS**
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay // Para un posible retraso si es necesario

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val ACTION_START_MQTT_SERVICE = "com.modulap.nidosano.START_MQTT_SERVICE"
    }

    private lateinit var authViewModel: AuthViewModel

    // Bandera para asegurar que solo publicamos el userId una vez por sesión iniciada
    // private var hasPublishedUserIdToEsp: Boolean = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(AuthViewModel::class.java)

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

                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
                val userIdFromPrefs by authViewModel.userIdFlow.collectAsState()

                // LaunchedEffect para iniciar el servicio MQTT y publicar el userId al ESP32
                /*LaunchedEffect(isAuthenticated, userIdFromPrefs) {
                    val userIdToUse = if (isAuthenticated) userIdFromPrefs else null

                    if (userIdToUse != null) {
                        Log.d(TAG, "Auth state changed or userId available. Starting MQTT service with userId: $userIdToUse")
                        startMqttService(userIdToUse)

                        // **** NUEVA LÓGICA: Publicar el userId al ESP32 ****
                        // Solo si no lo hemos publicado ya en esta sesión
                        if (!hasPublishedUserIdToEsp) {
                            Log.d(TAG, "Esperando conexión MQTT para publicar userId a ESP32...")
                            // Esperar a que el cliente MQTT esté CONECTADO para el userId actual
                            MQTTManagerHiveMQ.connectionStateFlow
                                .filter { (state, _) ->
                                    state == MQTTManagerHiveMQ.ConnectionState.CONNECTED && MQTTManagerHiveMQ.currentUserId == userIdToUse
                                }
                                .collect {
                                    Log.d(TAG, "Cliente MQTT conectado con userId ($userIdToUse). Publicando UserID al ESP32: $userIdToUse")
                                    // Usar la función publicar de MQTTManagerHiveMQ al tópico GLOBAL
                                    MQTTManagerHiveMQ.publicar(
                                        MQTTManagerHiveMQ.GLOBAL_USER_ID_CONFIG_TOPIC, // El tópico global
                                        userIdToUse // El userId como payload
                                    )
                                    hasPublishedUserIdToEsp = true // Marcar como publicado
                                    Log.d(TAG, "UserID '$userIdToUse' publicado a ${MQTTManagerHiveMQ.GLOBAL_USER_ID_CONFIG_TOPIC}.")
                                    // No cancelar la recolección aquí para permitir reintentos si la conexión se pierde y se recupera,
                                    // pero usar la bandera `hasPublishedUserIdToEsp` para evitar publicaciones redundantes
                                    // si el efecto se vuelve a ejecutar sin un cambio real de sesión.
                                }
                        }
                    } else {
                        Log.d(TAG, "User not authenticated or userId not available. MQTT service will not start with a specific userId yet.")
                        hasPublishedUserIdToEsp = false // Reiniciar la bandera si el usuario no está logueado
                    }
                }



                // Reiniciar la bandera `hasPublishedUserIdToEsp` cuando el usuario se desautentica
                LaunchedEffect(isAuthenticated) {
                    if (!isAuthenticated) {
                        hasPublishedUserIdToEsp = false
                        Log.d(TAG, "Usuario desautenticado. Bandera hasPublishedUserIdToEsp reiniciada a false.")
                    }
                }

                 */

                LaunchedEffect(isAuthenticated, userIdFromPrefs) {
                    val userIdToUse = if (isAuthenticated) userIdFromPrefs else null

                    if (userIdToUse != null) {
                        Log.d(TAG, "Auth state changed or userId available in MainActivity. Starting MqttForegroundService with userId: $userIdToUse")
                        startMqttService(userIdToUse)
                    } else {
                        // Si no hay usuario autenticado, detener el servicio
                        Log.d(TAG, "User not authenticated. Stopping MqttForegroundService.")
                        stopMqttService()
                    }
                }

                NidoSanoAppWrapper(
                    startDestination = destinationFromNotification,
                    authViewModel = authViewModel // Pasar authViewModel al wrapper si es necesario para acceder al userId en NidoSanoApp
                )
            }
        }
    }

    private fun startMqttService(userId: String) {
        val serviceIntent = Intent(this, MqttForegroundService::class.java).apply {
            action = ACTION_START_MQTT_SERVICE
            putExtra(MqttForegroundService.EXTRA_USER_ID, userId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopMqttService() {
        val serviceIntent = Intent(this, MqttForegroundService::class.java)
        stopService(serviceIntent)
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NidoSanoAppWrapper(startDestination: String?, authViewModel: AuthViewModel) { // Recibir authViewModel aquí
    val userId by authViewModel.userIdFlow.collectAsState() // Obtener userId del AuthViewModel
    NidoSanoApp(startDestination = startDestination, userId = userId)
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

    LaunchedEffect(userId) {
        if (userId == null) {
            // Si no hay userId, ir a la pantalla de login (o Splash, que lleva a login)
            if (navController.currentDestination?.route != Routes.Login && navController.currentDestination?.route != Routes.Splash) {
                navController.navigate(Routes.Login) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        } else {
            // Si hay userId y el destino no es el de la notificación,
            // asegurar que no se quede en Splash/Login si ya está autenticado.
            if (actualStart == Routes.Splash || actualStart == Routes.Login) {
                // Si el usuario ya está logueado, navega a Home
                if (navController.currentDestination?.route == Routes.Splash || navController.currentDestination?.route == Routes.Login) {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                // Si hay un destino de notificación, navegar a él
                if (navController.currentDestination?.route != actualStart) {
                    navController.navigate(actualStart) {
                        popUpTo(defaultStart) { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
        composable(Routes.FeedingSchedule) {
            ScheduleFeedingScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.FeedingScheduleList) {
            ScheduleListScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "feeding_schedule_edit/{scheduleId}/{initialHour}/{initialMinute}/{initialQuantityGrams}/{initialFrequency}",
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType },
                navArgument("initialHour") { type = NavType.IntType },
                navArgument("initialMinute") { type = NavType.IntType },
                navArgument("initialQuantityGrams") { type = NavType.IntType },
                navArgument("initialFrequency") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            val initialHour = backStackEntry.arguments?.getInt("initialHour")
            val initialMinute = backStackEntry.arguments?.getInt("initialMinute")
            val initialQuantityGrams = backStackEntry.arguments?.getInt("initialQuantityGrams")
            val initialFrequency = backStackEntry.arguments?.getString("initialFrequency")

            Log.d("NidoSanoApp", "Editing schedule. ID: $scheduleId, Hour: $initialHour, Freq: $initialFrequency")

            ScheduleFeedingScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                scheduleId = scheduleId,
                initialHour = initialHour,
                initialMinute = initialMinute,
                initialQuantityGrams = initialQuantityGrams,
                initialFrequency = initialFrequency
            )
        }
        composable(Routes.Home) {
            MonitoringScreen(
                navController = navController,
                viewModel = sharedMqttViewModel
            )
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

        composable(Routes.SetMonitoringSchedule){
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
        SetMonitoringScheduleScreen(
            navController = navController,
            viewModel = securityViewModel,
            onBackClick = { navController.popBackStack() })
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
            } else {
                Log.e("NidoSanoApp", "userId es nulo para EditProfileScreen")
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Home) { inclusive = true }
                }
            }
        }
        composable(Routes.Password) {
            EditPasswordScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() })
        }

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
            } else {
                Log.e("NidoSanoApp", "userId es nulo para NotificationScreen")
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Home) { inclusive = true }
                }
            }
        }
        composable(Routes.History) {
            if (userId != null) {
                HistorialScreen(
                    navController = navController,
                    onBackClick = { navController.popBackStack() },
                    userId = userId,
                    coopId = "defaultChickenCoop",
                    onViewMoreClick = { date ->
                        navController.navigate("history_detail_screen/$date")
                    }
                )
            } else {
                Log.e("NidoSanoApp", "userId es nulo para HistorialScreen")
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Home) { inclusive = true }
                }
            }
        }
        composable(
            route = Routes.DetailWithArgs,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            if (userId != null) {
                HistorialDetailScreen(
                    userId = userId,
                    coopId = "defaultChickenCoop",
                    date = date,
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Log.e("NidoSanoApp", "userId es nulo para HistorialDetailScreen")
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Home) { inclusive = true }
                }
            }
        }
    }
}