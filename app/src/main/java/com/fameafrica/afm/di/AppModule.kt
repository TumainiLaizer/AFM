package com.fameafrica.afm.di

import android.content.Context
import com.fameafrica.afm.data.database.AFMDatabase
import com.fameafrica.afm.data.database.CareerDatabaseProvider
import com.fameafrica.afm.data.database.RoomDataImporter
import com.fameafrica.afm.data.database.dao.*
import com.fameafrica.afm.data.initializer.GameInitializer
import com.fameafrica.afm.domain.manager.PlayerGenerator
import com.fameafrica.afm.utils.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)

    // ============ SETTINGS MANAGER ============

    @Provides
    @Singleton
    fun provideSettingsManager(
        gameSettingsDao: GameSettingsDao,
        regionalSettingsDao: RegionalSettingsDao,
        currencyExchangeRatesDao: CurrencyExchangeRatesDao,
        settingsHistoryDao: SettingsHistoryDao,
        userAnalyticsDao: UserAnalyticsDao,
        userPreferencesDao: UserPreferencesDao,
        @ApplicationContext context: Context,
    ): SettingsManager {
        return SettingsManager(
            gameSettingsDao = gameSettingsDao,
            regionalSettingsDao = regionalSettingsDao,
            currencyExchangeRatesDao = currencyExchangeRatesDao,
            settingsHistoryDao = settingsHistoryDao,
            userPreferencesDao = userPreferencesDao,
            context = context,
            userAnalyticsDao = userAnalyticsDao
        )
    }

    // ============ GAME INITIALIZER ============

    @Provides
    @Singleton
    fun provideGameInitializer(
        databaseProvider: CareerDatabaseProvider,
        importer: RoomDataImporter,
        playerGenerator: PlayerGenerator,
        staffGenerator: com.fameafrica.afm.domain.manager.StaffGenerator
    ): GameInitializer {
        return GameInitializer(
            databaseProvider = databaseProvider,
            importer = importer,
            playerGenerator = playerGenerator,
            staffGenerator = staffGenerator
        )
    }
}
