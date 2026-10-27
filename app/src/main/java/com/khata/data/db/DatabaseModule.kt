package com.khata.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS accounts (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                institution TEXT NOT NULL,
                accountType TEXT NOT NULL,
                openingBalancePaise INTEGER NOT NULL,
                isActive INTEGER NOT NULL,
                isLinkedViaAa INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                PRIMARY KEY(id)
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO accounts (id, name, institution, accountType, openingBalancePaise, isActive, isLinkedViaAa, createdAt)
            VALUES ('manual', 'Manual', 'Manual', 'CASH', 0, 1, 0, strftime('%Y-%m-%d', 'now'))
        """.trimIndent())
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_normalizedMerchant ON transactions(normalizedMerchant)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KhataDatabase =
        Room.databaseBuilder(context, KhataDatabase::class.java, "khata.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideDao(database: KhataDatabase): KhataDao = database.dao()

    @Provides @Singleton
    fun provideAppContext(@ApplicationContext context: Context): Context = context
}
