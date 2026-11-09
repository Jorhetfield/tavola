package com.tavola

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.feature.auth.AuthGate
import com.tavola.feature.groups.GroupGate
import com.tavola.feature.widget.TodayWidgetRefresh
import com.tavola.navigation.TavolaNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /** The recipe a widget asked to open, until the navigation has opened it. */
    private var recipeToOpen: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a fresh launch reads the intent: when the system restores the activity, the screens come back with their state.
        if (savedInstanceState == null) recipeToOpen = intent.recipeId()
        setContent {
            TavolaTheme {
                AuthGate(webClientId = getString(R.string.default_web_client_id)) {
                    GroupGate {
                        TavolaNavigation(recipeToOpen = recipeToOpen, onRecipeOpened = { recipeToOpen = null })
                    }
                }
            }
        }
    }

    /** A widget was tapped while the app was already open: the activity is reused and gets the intent here. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recipeToOpen = intent.recipeId()
    }

    /** What may have changed in the app is drawn again in the widgets once the app is out of sight. */
    override fun onStop() {
        super.onStop()
        TodayWidgetRefresh.requestNow(applicationContext)
    }

    private fun Intent.recipeId(): String? = getStringExtra(EXTRA_RECIPE_ID)

    companion object {
        /** The id of the recipe the app has to open on, put by the widgets. */
        const val EXTRA_RECIPE_ID = "com.tavola.extra.RECIPE_ID"
    }
}
