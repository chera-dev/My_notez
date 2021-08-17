package com.example.mynotez.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mynotez.data.dao.LabelDao
import com.example.mynotez.data.dao.NoteDao
import com.example.mynotez.data.entities.Label
import com.example.mynotez.data.entities.Notes
import com.example.mynotez.data.typeconverters.SetOfLongTypeConverter
import com.example.mynotez.data.typeconverters.SetOfStringTypeConverter

@Database(entities = [Notes::class, Label::class], version = 1,exportSchema = false)
@TypeConverters(SetOfStringTypeConverter::class, SetOfLongTypeConverter::class)

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