/*
 * Copyright (c)  2026 Zakir Sheikh
 *
 * Created by sheik on 4 of Jan 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last Modified by sheik on 4 of Jan 2026
 *
 */

package com.zs.common.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zs.common.db.RoomDb.Companion.initialize
import com.zs.common.db.media.Album
import com.zs.common.db.media.Album.Memory
import com.zs.common.db.media.MediaFile
import com.zs.common.db.media.MediaProvider

@Database(
    version = 1,
    entities = [MediaFile::class, Album::class, Memory::class]
)
internal abstract class RoomDb : RoomDatabase() {

    /**
     * Represents the [Dao] for managing [Album]
     */
    abstract val mediaProvider: MediaProvider

    //
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RoomDb? = null

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
                        RoomDb::class.java,
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
        fun getInstance(): RoomDb =
            INSTANCE ?: error("Database instance missing — ensure initialization at app startup.")
    }
}