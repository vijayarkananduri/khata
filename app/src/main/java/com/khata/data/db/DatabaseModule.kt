package com.khata.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KhataDatabase =
        Room.databaseBuilder(context, KhataDatabase::class.java, "khata.db").build()

    @Provides
    fun provideDao(database: KhataDatabase): KhataDao = database.dao()

    @Provides @Singleton
    fun provideAppContext(@ApplicationContext context: Context): Context = context
}
