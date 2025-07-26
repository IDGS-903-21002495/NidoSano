package com.modulap.nidosano.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.theme.OrangePrimary

data class NavItem(val route: String, val icon: Int)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        NavItem("home", R.drawable.granja),
        NavItem("security", R.drawable.verificacion_de_escudo),
        NavItem("notification", R.drawable.campana),
        NavItem("feeding", R.drawable.trigo),
        NavItem("tips", R.drawable.gallina)
    )

    NavigationBar(
        tonalElevation = 1.dp,
        containerColor = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White)
    ) {
        items.forEach { item ->
            val selected = item.route == currentRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.route) },
                icon = {
                    val painter = painterResource(id = item.icon)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (selected) OrangePrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(12.dp)
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = item.route,
                            colorFilter = ColorFilter.tint(if (selected) Color.White else Color.Black),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                alwaysShowLabel = false,
                interactionSource = remember { MutableInteractionSource() },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Black
                )
            )
        }
    }
}
