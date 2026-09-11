package com.lumen.app.di

import com.lumen.app.core.locale.LocaleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ThemeManager, LumenStorageManager and BatteryPerformanceManager all have an @Inject
    // constructor (with @ApplicationContext-qualified Context), so Hilt provides them
    // automatically — no explicit @Provides needed here.

    @Provides
    @Singleton
    fun provideLocaleManager(): LocaleManager = LocaleManager()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
