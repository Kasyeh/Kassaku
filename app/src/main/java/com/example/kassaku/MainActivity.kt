package com.example.kassaku

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kassaku.ui.HomeScreen
import com.example.kassaku.ui.ImpianScreen
import com.example.kassaku.ui.LoginScreen
import com.example.kassaku.ui.ProfileScreen
import com.example.kassaku.ui.RiwayatScreen
import com.example.kassaku.ui.SplashScreen
import com.example.kassaku.ui.StatistikScreen
import com.example.kassaku.ui.theme.KasSakuTheme
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.utils.ForceLogoutManager
import com.example.kassaku.utils.ThemeMode
import com.example.kassaku.utils.ThemePreferences
import com.example.kassaku.viewmodel.HomeViewModel
import com.example.kassaku.ui.components.BottomNavItem
import com.example.kassaku.ui.components.KassakuBottomBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    // Request permission launcher untuk notifikasi
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
            getFCMToken()
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = ThemePreferences(this)
        val initialThemeMode = themePreferences.getThemeMode()
        
        // Request notification permission dan get FCM token
        askNotificationPermission()
        
        // Initialize Notification Channel
        createNotificationChannel()

        setContent {
            val homeViewModel: HomeViewModel = viewModel()
            val context = LocalContext.current
            
            // Initialize theme mode once before the first composition draw
            remember {
                if (homeViewModel.themeMode.value == ThemeMode.SYSTEM) {
                    homeViewModel.setThemeMode(initialThemeMode)
                }
                true
            }

            val themeMode by homeViewModel.themeMode.collectAsState()

            // Sync persistence when theme changes
            LaunchedEffect(themeMode) {
                themePreferences.setThemeMode(themeMode)
            }

            KasSakuTheme(themeMode = themeMode) {
                KasSakuApp(
                    homeViewModel = homeViewModel,
                    onSyncFCM = { userId -> getFCMToken(userId) }
                )
            }
        }
    }
    
    /**
     * Request notification permission untuk Android 13+
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permission sudah granted, langsung get token
                getFCMToken()
            }
        } else {
            // Android < 13 tidak perlu permission runtime untuk notifikasi
            getFCMToken()
        }
    }
    
    /**
     * Ambil FCM token dan kirim ke server jika user sudah login atau userId diberikan
     */
    private fun getFCMToken(providedUserId: Int? = null) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")
            
            // Prioritaskan providedUserId, jika tidak ada baru ambil dari SharedPreferences
            val userId = if (providedUserId != null && providedUserId != -1) {
                providedUserId
            } else {
                val sharedPref = this@MainActivity.getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)
                sharedPref.getInt("user_id", -1)
            }
            
            if (userId != -1) {
                this@MainActivity.sendTokenToServer(token, userId)
            }
        }
    }

    /**
     * Kirim FCM token ke backend Laravel
     */
    private fun sendTokenToServer(token: String, userId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = com.example.kassaku.data.remote.ApiClient.api.saveFcmToken(token, userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("MainActivity", "FCM token synced to server")
                } else {
                    Log.e("MainActivity", "Failed to sync token: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error syncing token: ${e.message}")
            }
        }
    }

    /**
     * Buat notification channel (wajib untuk Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "kassaku_notifications"
            val channelName = "Kassaku Notifications"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi dari Kassaku"
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("MainActivity", "Notification channel created: $channelId")
        }
    }
}

// BottomNavItem sealed class moved to ui/components/KassakuBottomNavigation.kt

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val SPLASH_ROUTE = "splash"
    const val REGISTER_ROUTE = "register" // Rute registrasi baru
    const val MAIN_APP_ROUTE = "main_app" // Rute baru untuk bagian utama aplikasi setelah login

    // Rute untuk Bottom Navigation (digunakan sebagai start destination di inner NavHost)
    const val HOME_ROUTE = "home_bottom_nav" // Ubah nama agar tidak konflik dengan argumen
    const val IMPIAN_ROUTE = "impian_bottom_nav"
    const val RIWAYAT_ROUTE = "riwayat_bottom_nav"
    const val STATISTIK_ROUTE = "statistik_bottom_nav"
    const val PROFIL_ROUTE = "profil_bottom_nav"

    const val USER_ID_ARG = "userId"
}

@Composable
fun KasSakuApp(homeViewModel: HomeViewModel, onSyncFCM: (Int?) -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()
    // NavHost utama: mengelola navigasi antara Login dan MainApp (setelah login)
    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH_ROUTE
    ) {
        composable(AppDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onSplashFinished = {
                    val sharedPref = context.getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)
                    val userId = sharedPref.getInt("user_id", -1)
                    val isRemembered = sharedPref.getBoolean("remember_me", false)
                    val savedToken = sharedPref.getString("auth_token", null)
                    
                    if (userId != -1 && isRemembered && savedToken != null) {
                        // Restore token ke ApiClient
                        com.example.kassaku.data.remote.ApiClient.setToken(savedToken)
                        navController.navigate("${AppDestinations.MAIN_APP_ROUTE}/$userId") {
                            popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = { userId, rememberMe ->
                    // Simpan userId dan token ke SharedPreferences
                    val sharedPref = context.getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)
                    val token = com.example.kassaku.data.remote.ApiClient.getToken()
                    sharedPref.edit()
                        .putInt("user_id", userId)
                        .putBoolean("remember_me", rememberMe)
                        .putString("auth_token", token)
                        .apply()
                    
                    // Trigger sync token
                    onSyncFCM(userId)
                    
                    // Navigasi ke MainAppScreen dan teruskan userId
                    navController.navigate("${AppDestinations.MAIN_APP_ROUTE}/$userId") {
                        popUpTo(AppDestinations.LOGIN_ROUTE) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER_ROUTE)
                },
                onSyncFCM = onSyncFCM
            )
        }
        composable(AppDestinations.REGISTER_ROUTE) {
            com.example.kassaku.ui.RegisterScreen(
                onRegisterSuccess = { userId ->
                    // Simpan userId dan token ke SharedPreferences
                    val sharedPref = context.getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)
                    val token = com.example.kassaku.data.remote.ApiClient.getToken()
                    sharedPref.edit()
                        .putInt("user_id", userId)
                        .putString("auth_token", token)
                        .apply()
                    
                    // Trigger sync token
                    onSyncFCM(userId)
                    
                    // Langsung masuk setelah register sukses
                    navController.navigate("${AppDestinations.MAIN_APP_ROUTE}/$userId") {
                        popUpTo(AppDestinations.LOGIN_ROUTE) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSyncFCM = onSyncFCM
            )
        }
        composable(
            route = "${AppDestinations.MAIN_APP_ROUTE}/{${AppDestinations.USER_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.USER_ID_ARG) { type = NavType.IntType })
        ) {
            backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(AppDestinations.USER_ID_ARG)
            if (userId != null) {
                MainAppScreen(
                    userId = userId,
                    homeViewModel = homeViewModel,
                    onLogout = { reason ->
                        // Clear SharedPreferences dan token
                        val sharedPref = context.getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit().remove("user_id").remove("remember_me").remove("auth_token").apply()
                        com.example.kassaku.data.remote.ApiClient.clearToken()

                        if (reason == com.example.kassaku.viewmodel.LogoutReason.BLOCKED) {
                           Toast.makeText(context, "Akun Anda telah dinonaktifkan oleh admin", Toast.LENGTH_LONG).show()
                        }

                        // Logout: Pop backstack sampai Login
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(AppDestinations.MAIN_APP_ROUTE) { inclusive = true }
                        }
                    }
                )
            } else {
                // Jika userId null, kembali ke login (seharusnya tidak terjadi jika navigasi benar)
                navController.popBackStack(AppDestinations.LOGIN_ROUTE, false)
            }
        }
    }
}

@Composable
fun MainAppScreen(userId: Int, homeViewModel: HomeViewModel, onLogout: (com.example.kassaku.viewmodel.LogoutReason) -> Unit) {
    val bottomNavController = rememberNavController()
    val context = LocalContext.current
    var lastBackPressTime by remember { androidx.compose.runtime.mutableStateOf(0L) }

    androidx.compose.runtime.LaunchedEffect(userId) {
        homeViewModel.startSessionMonitoring(userId)
    }

    androidx.activity.compose.BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
    }

    androidx.compose.runtime.LaunchedEffect(homeViewModel) {
        homeViewModel.logoutNavigationEvent.collectLatest { reason ->
            homeViewModel.resetRealtimeStateAfterLogout()
            onLogout(reason)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        ForceLogoutManager.events.collectLatest { reason ->
            homeViewModel.resetRealtimeStateAfterLogout()
            onLogout(reason)
        }
    }

    Scaffold(
        bottomBar = {
            KassakuBottomBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        // NavHost untuk konten Bottom Navigation
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route, // Rute default untuk bottom nav
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    userId = userId, 
                    homeViewModel = homeViewModel, 
                    navController = bottomNavController,
                    onLogout = { onLogout(com.example.kassaku.viewmodel.LogoutReason.MANUAL) }
                )
            }
            composable(BottomNavItem.Impian.route) {
                ImpianScreen(
                    userId = userId, 
                    homeViewModel = homeViewModel,
                    onLogout = { onLogout(com.example.kassaku.viewmodel.LogoutReason.MANUAL) }
                )
            }
            composable(BottomNavItem.Riwayat.route) {
                RiwayatScreen(
                    userId = userId, 
                    homeViewModel = homeViewModel,
                    onLogout = { onLogout(com.example.kassaku.viewmodel.LogoutReason.MANUAL) }
                )
            }
            composable(BottomNavItem.Statistik.route) {
                StatistikScreen(
                    userId = userId,
                    homeViewModel = homeViewModel,
                    onLogout = { onLogout(com.example.kassaku.viewmodel.LogoutReason.MANUAL) },
                    onNavigateToImpian = {
                        bottomNavController.navigate(BottomNavItem.Impian.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Profil.route) {
                ProfileScreen(
                    userId = userId,
                    homeViewModel = homeViewModel,
                    onLogout = { onLogout(com.example.kassaku.viewmodel.LogoutReason.MANUAL) }
                )
            }
        }
    }
}

// Old BottomNavigationBar removed. Using reusable KassakuBottomBar instead.
