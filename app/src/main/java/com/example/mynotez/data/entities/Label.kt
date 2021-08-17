package com.example.mynotez.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "label_table")
data class Label(@PrimaryKey @ColumnInfo(name = "label_name") var labelName:String,
                 @ColumnInfo(name = "notes") private val noteIds: MutableSet<Long> = mutableSetOf())
    :Serializable{

    @ColumnInfo(name = "order") var order:Int = 0

    fun addNote(noteId:Long){
        noteIds.add(noteId)
    }

    fun removeNote(note:Long){
        noteIds.remove(note)
    }

    //if it is private and val need below getter function
    fun getNoteIds():Set<Long>{
        return noteIds
    }

}


