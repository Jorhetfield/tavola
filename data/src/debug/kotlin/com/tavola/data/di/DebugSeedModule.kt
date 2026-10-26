package com.tavola.data.di

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tavola.data.TavolaDatabase
import com.tavola.data.recipe.MediterraneanCatalog
import com.tavola.data.recipe.toEntity
import com.tavola.domain.dispatcher.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Debug builds fill a freshly created database with the Mediterranean sample recipes; release builds start empty. */
@Module
@InstallIn(SingletonComponent::class)
internal object DebugSeedModule {
    @Provides
    @IntoSet
    fun provideSeedCallback(database: Provider<TavolaDatabase>, @IoDispatcher ioDispatcher: CoroutineDispatcher): RoomDatabase.Callback =
        object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                // The database is still being created here, so it is reached through a Provider on another coroutine.
                CoroutineScope(SupervisorJob() + ioDispatcher).launch {
                    database.get().recipeDao().upsertAll(MediterraneanCatalog.recipes.map { recipe -> recipe.toEntity() })
                }
            }
        }
}
