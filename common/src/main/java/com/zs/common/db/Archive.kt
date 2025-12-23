/*
 * Copyright (c)  2025 Zakir Sheikh
 *
 * Created by sheik on 23 of Dec 2025
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
 * Last Modified by sheik on 23 of Dec 2025
 */

package com.zs.common.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zs.common.db.albums.Album
import com.zs.common.db.albums.Album.Memory
import com.zs.common.db.albums.Albums
import com.zs.common.db.albums.MediaFile

@Database(
    entities = [MediaFile::class, Album::class, Memory::class],
    version = 1,
    exportSchema = false
)
internal abstract class Archive : RoomDatabase() {
    abstract val albums: Albums

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: Archive? = null
        operator fun invoke(context: Context): Archive {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, Archive::class.java, "db_archive"
                )
                    //.addCallback(CALLBACK)
                    //.fallbackToDestructiveMigrationFrom(0, 1, 2)
                    //.addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
