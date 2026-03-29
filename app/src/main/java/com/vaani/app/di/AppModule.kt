package com.vaani.app.di

import android.content.Context
import com.vaani.app.data.db.TaskDao
import com.vaani.app.data.db.VaaniDatabase
import com.vaani.app.core.accessibility.ActionDispatcher
import com.vaani.app.core.accessibility.ActionDispatcherImpl
import com.vaani.app.core.accessibility.SmartElementFinder
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
    fun provideDatabase(@ApplicationContext context: Context): VaaniDatabase {
        return VaaniDatabase.getDatabase(context)
    }

    @Provides
    fun provideTaskDao(database: VaaniDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideActionDispatcher(smartElementFinder: SmartElementFinder): ActionDispatcher {
        return ActionDispatcherImpl(smartElementFinder)
    }
}
