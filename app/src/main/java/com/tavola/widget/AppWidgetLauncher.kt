package com.tavola.widget

import android.content.Context
import android.content.Intent
import com.tavola.MainActivity
import com.tavola.feature.widget.WidgetLauncher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * How the widgets open the app. The widget module does not know the screens, so `:app` builds the intents: they open [MainActivity]
 * on the screen they ask for, and reuse the app if it is already open.
 */
class AppWidgetLauncher @Inject constructor(@ApplicationContext private val context: Context) : WidgetLauncher {
    override fun openAppIntent(): Intent = baseIntent()

    override fun openRecipeIntent(recipeId: String): Intent = baseIntent().putExtra(MainActivity.EXTRA_RECIPE_ID, recipeId)

    private fun baseIntent(): Intent = Intent(context, MainActivity::class.java).addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP,
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    abstract fun bindWidgetLauncher(impl: AppWidgetLauncher): WidgetLauncher
}
