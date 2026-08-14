package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocumentRecord::class,
        BookmarkRecord::class,
        AnnotationRecord::class,
        OcrRecord::class,
        ConversionRecord::class,
        DocumentMetadataRecord::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun ocrDao(): OcrDao
    abstract fun conversionDao(): ConversionDao
    abstract fun documentMetadataDao(): DocumentMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lsdocs_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
