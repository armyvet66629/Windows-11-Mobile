package com.example.windows11mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.windows11mobile.data.SettingsRepository
import com.example.windows11mobile.data.RealSettingsRepository
import com.example.windows11mobile.navigation.Dest
import com.example.windows11mobile.ui.shell.MainShell
import com.example.windows11mobile.ui.theme.Windows11MobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val settingsRepository = RealSettingsRepository(this)
        
        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = null)
            val darkTheme = isDarkMode ?: androidx.compose.foundation.isSystemInDarkTheme()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme }
                )
                onDispose {}
            }
            
            Windows11MobileTheme(darkTheme = darkTheme) {
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
}
