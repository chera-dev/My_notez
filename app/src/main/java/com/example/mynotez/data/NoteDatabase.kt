package com.example.mynotez.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mynotez.data.Label
import com.example.mynotez.data.LabelTypeConverter
import com.example.mynotez.data.NoteTypeConverter
import com.example.mynotez.data.Notes

@Database(entities = [Notes::class, Label::class], version = 1,exportSchema = false)
@TypeConverters(LabelTypeConverter::class, NoteTypeConverter::class)

abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}