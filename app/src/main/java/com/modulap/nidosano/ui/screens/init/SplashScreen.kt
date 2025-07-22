package com.modulap.nidosano.ui.screens.init

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController){
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Ya hay un usuario autenticado
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Splash) { inclusive = true }
            }
        } else {
            // No hay sesión activa
            navController.navigate(Routes.Login) {
                popUpTo(Routes.Splash) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangePrimary),
        contentAlignment = Alignment.Center
    ){

        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(id = R.drawable.gallina),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "Logo NidoSano",
                modifier = Modifier.size(180.dp),
            )

            Text(
                "Nido sano",
                color = White,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }



    }
}