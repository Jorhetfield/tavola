package com.tavola

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.feature.auth.AuthGate
import com.tavola.navigation.TavolaNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TavolaTheme {
                AuthGate(webClientId = getString(R.string.default_web_client_id)) {
                    TavolaNavigation()
                }
            }
        }
    }
}
