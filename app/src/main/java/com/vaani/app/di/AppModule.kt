package com.vaani.app.di

import android.content.Context
import androidx.room.Room
import com.vaani.app.data.local.TaskDao
import com.vaani.app.data.local.VaaniDatabase
import com.vaani.app.service.ActionExecutor
import com.vaani.app.service.GeminiClient
import com.vaani.app.service.TTSManager
import com.vaani.app.service.VoiceRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVaaniDatabase(
        @ApplicationContext context: Context
    ): VaaniDatabase {
        return Room.databaseBuilder(
            context,
            VaaniDatabase::class.java,
            VaaniDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: VaaniDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideGeminiClient(): GeminiClient {
        return GeminiClient()
    }

    @Provides
    @Singleton
    fun provideVoiceRecognizer(
        @ApplicationContext context: Context
    ): VoiceRecognizer {
        return VoiceRecognizer(context)
    }

    @Provides
    @Singleton
    fun provideTTSManager(
        @ApplicationContext context: Context
    ): TTSManager {
        return TTSManager(context)
    }

    @Provides
    @Singleton
    fun provideActionExecutor(): ActionExecutor {
        return ActionExecutor()
    }
}
