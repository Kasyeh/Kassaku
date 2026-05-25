package com.example.kassaku.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kassaku.AppDestinations
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchTextPrimary
import com.example.kassaku.ui.theme.StitchTextSecondary

/**
 * Sealed class defining the Bottom Navigation Items.
 * Keeps the structured data separate for cleaner usage.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = AppDestinations.HOME_ROUTE, 
        title = "Home", 
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Filled.Home
    )
    object Impian : BottomNavItem(
        route = AppDestinations.IMPIAN_ROUTE, 
        title = "Tabungan", 
        selectedIcon = Icons.Rounded.Paid,
        unselectedIcon = Icons.Filled.Paid
    )
    object Statistik : BottomNavItem(
        route = AppDestinations.STATISTIK_ROUTE, 
        title = "Ringkasan", 
        selectedIcon = Icons.Rounded.PieChart,
        unselectedIcon = Icons.Filled.PieChart
    )
    object Riwayat : BottomNavItem(
        route = AppDestinations.RIWAYAT_ROUTE, 
        title = "Catatan", 
        selectedIcon = Icons.Rounded.History,
        unselectedIcon = Icons.Filled.History
    )
    object Profil : BottomNavItem(
        route = AppDestinations.PROFIL_ROUTE, 
        title = "Profil", 
        selectedIcon = Icons.Rounded.AccountCircle,
        unselectedIcon = Icons.Filled.AccountCircle
    )
}

/**
 * Modern Bottom Navigation Bar with animated indicators and Material 3 styling.
 * 
 * Features:
 * - Floating/Elevated appearance with shadow
 * - "Pill" style active indicator
 * - Smooth scale animations for icons
 * - Clean typography
 */
@Composable
fun KassakuBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Statistik,
        BottomNavItem.Riwayat,
        BottomNavItem.Impian,
        BottomNavItem.Profil
    )

    // Parent box for shadow/elevation
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp) // Floating effect
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp) // standard comfortable height
                .clip(RoundedCornerShape(24.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            tonalElevation = 0.dp // We handle elevation manually
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                // Animated Color State
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) StitchPrimary else StitchTextSecondary,
                    label = "iconColor"
                )

                // Animated Scale State
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "iconScale"
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale),
                            tint = iconColor
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = iconColor,
                            maxLines = 1
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StitchPrimary,
                        selectedTextColor = StitchPrimary,
                        indicatorColor = StitchPrimary.copy(alpha = 0.1f), // Subtle pill
                        unselectedIconColor = StitchTextSecondary,
                        unselectedTextColor = StitchTextSecondary
                    ),
                    alwaysShowLabel = true // Clean look, always showing label helps UX
                )
            }
        }
    }
}
