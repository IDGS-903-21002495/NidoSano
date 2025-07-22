package com.modulap.nidosano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.screens.auth.LoginScreen
import com.modulap.nidosano.ui.screens.init.SplashScreen
import com.modulap.nidosano.ui.screens.monitoring.MonitoringScreen
import com.modulap.nidosano.ui.theme.NidoSanoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = Color.White,
                    surface = Color.White
                )
            ) {
                NidoSanoApp()
            }
        }
    }
}

// Navegación

@Composable
fun NidoSanoApp(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash){
        composable(Routes.Splash) { SplashScreen(navController) }
        composable(Routes.Login) { LoginScreen(navController) }
        composable(Routes.Home) { MonitoringScreen(navController) }
    }
}