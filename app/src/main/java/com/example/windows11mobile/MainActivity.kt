package com.example.windows11mobile

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.data.RealSettingsRepository
import com.example.windows11mobile.data.ContactsRepository
import com.example.windows11mobile.navigation.Dest
import com.example.windows11mobile.ui.shell.MainShell
import com.example.windows11mobile.ui.theme.Windows11MobileTheme

import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Request location permissions for Weather
        // Request location permissions for Weather
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.any { it }) {
                // Re-register observers once permissions are granted
                ContactsRepository.getInstance(this).registerObservers()
                
                // Force an immediate update now that we have permissions
                CoroutineScope(Dispatchers.IO).launch {
                    ContactsRepository.getInstance(this@MainActivity).updateRecentActivity()
                    ContactsRepository.getInstance(this@MainActivity).updateContacts()
                    // homeViewModel instance is needed here or we use the provider again
                }
                
                // Use the provider to get the same instance and refresh photos
                val settingsRepository = RealSettingsRepository(this)
                val rssRepository = com.example.windows11mobile.data.RssRepository()
                val homeViewModel = androidx.lifecycle.ViewModelProvider(
                    this,
                    com.example.windows11mobile.ui.home.HomeViewModelFactory(settingsRepository, rssRepository, application)
                )[com.example.windows11mobile.ui.home.HomeViewModel::class.java]
                homeViewModel.refreshPhotos()
            }
        }

        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS
        )
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
        
        val settingsRepository = RealSettingsRepository(this)
        val rssRepository = com.example.windows11mobile.data.RssRepository()
        val homeViewModel = androidx.lifecycle.ViewModelProvider(
            this,
            com.example.windows11mobile.ui.home.HomeViewModelFactory(settingsRepository, rssRepository, application)
        )[com.example.windows11mobile.ui.home.HomeViewModel::class.java]
        
        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = null)
            val accentColorInt by settingsRepository.accentColor.collectAsStateWithLifecycle(initialValue = SettingsRepository.DEFAULT_ACCENT_COLOR)
            val statusBarMode by settingsRepository.statusBarMode.collectAsStateWithLifecycle(initialValue = "auto")
            val darkTheme = isDarkMode ?: androidx.compose.foundation.isSystemInDarkTheme()

            DisposableEffect(darkTheme, statusBarMode) {
                val statusBarStyle = when (statusBarMode) {
                    "light" -> SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                    "dark" -> SystemBarStyle.dark(
                        android.graphics.Color.TRANSPARENT
                    )
                    else -> SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme }
                }

                enableEdgeToEdge(
                    statusBarStyle = statusBarStyle,
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme }
                )
                onDispose {}
            }
            
            Windows11MobileTheme(
                darkTheme = darkTheme,
                accentColor = Color(accentColorInt)
            ) {
                val backStack = rememberNavBackStack(Dest.Desktop)
                
                MainShell(
                    backStack = backStack,
                    settingsRepository = settingsRepository,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        } else {
                            finish()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Improve "Go Home" transition feel by removing activity enter/exit animations
        // so the OS-level gesture animation takes center stage without interference
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Check if this is a Home button press or a re-launch of the main activity
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            val settingsRepository = RealSettingsRepository(this)
            val rssRepository = com.example.windows11mobile.data.RssRepository()
            val homeViewModel = androidx.lifecycle.ViewModelProvider(
                this,
                com.example.windows11mobile.ui.home.HomeViewModelFactory(settingsRepository, rssRepository, application)
            )[com.example.windows11mobile.ui.home.HomeViewModel::class.java]
            homeViewModel.onHomeButtonPressed()
        }
    }
}
