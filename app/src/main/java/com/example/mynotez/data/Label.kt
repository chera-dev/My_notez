package com.example.mynotez.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import java.io.Serializable

@Entity(tableName = "label_table")
data class Label(@PrimaryKey @ColumnInfo(name = "label_name") var labelName:String,
                 @ColumnInfo(name = "notes") private val notes: MutableSet<Long> = mutableSetOf())
    :Serializable{

    @ColumnInfo(name = "order") var order:Int = 0

    fun addNote(noteId:Long){
        notes.add(noteId)
    }

    fun removeNotes(note:Long){
        notes.remove(note)
    }

    //if it is private and val need below getter function
    fun getNotes():Set<Long>{
        return notes
    }

    /*fun getNotesId():Set<Long>{
        val notes = mutableSetOf<Long>()
        for (i in this.notes)
            notes.add(i.noteId)
        return notes
    }*/

}


class NoteTypeConverter {

    @TypeConverter
    fun listToJson(value: MutableSet<Long>?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toMutableSet()
}

