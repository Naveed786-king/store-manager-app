package com.storemanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.storemanager.app.ui.navigation.StoreManagerNavHost
import com.storemanager.app.ui.theme.StoreManagerTheme
import com.storemanager.app.util.PrefsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefsManager = PrefsManager(applicationContext)
            val darkMode by prefsManager.darkMode.collectAsState(initial = false)
            StoreManagerTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StoreManagerNavHost()
                }
            }
        }
    }
}
