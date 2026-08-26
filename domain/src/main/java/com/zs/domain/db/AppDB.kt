package com.zs.domain.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zs.domain.db.AppDb.Companion.initialize
import com.zs.domain.db.media.Album
import com.zs.domain.db.media.Album.Memory
import com.zs.domain.db.media.MediaFile
import com.zs.domain.db.media.MediaProvider

@Database(
    version = 1,
    entities = [MediaFile::class, Album::class, Memory::class]
)
internal abstract class AppDb : RoomDatabase() {

    /**
     * Represents the [Dao] for managing [Album]
     */
    abstract val mediaProvider: MediaProvider

    //
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDb? = null

        /**
         * Initializes the singleton database instance.
         *
         * This function should be called once, typically during application startup, to create the
         * database. It uses a singleton pattern to ensure only one instance of the database is
         * ever created. If the instance already exists, it returns the existing instance.
         *
         * @param context The application context, used to create the database.
         * @return The singleton [AppDb] instance.
         */
        fun initialize(context: Context) {
            // Check if the database instance is already created.
            if (INSTANCE != null) return
            // If not, enter a synchronized block to ensure thread-safe initialization.
            synchronized(this) {
                // Double-check inside synchronized block to avoid race conditions.
                if (INSTANCE == null) {
                    // Build the Room database using the application context.
                    // This ensures we don’t accidentally leak an Activity or Service context.
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDb::class.java,
                        "app_db"
                    )
                        //.addCallback(CALLBACK) // Optional: run custom logic when DB is created/opened
                        //.fallbackToDestructiveMigrationFrom(0, 1, 2) // Optional: wipe DB for specific old versions
                        //.addMigrations(MIGRATION_3_4, MIGRATION_4_5) // Optional: handle schema upgrades safely
                        .build()
                }
            }
        }

        /**
         * Returns the singleton [AppDb] instance.
         *
         * If the instance is not yet created, this function will throw an [IllegalArgumentException].
         * You must call [initialize] at application startup before calling this method.
         *
         * @return The singleton instance of [AppDb].
         * @throws IllegalArgumentException if the database has not been initialized.
         */
        fun getInstance(): AppDb =
            INSTANCE ?: error("Database instance missing — ensure initialization at app startup.")
    }
}