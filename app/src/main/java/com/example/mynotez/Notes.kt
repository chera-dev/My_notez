package com.example.mynotez

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Entity(tableName = "note_table")
data class Notes(@ColumnInfo(name = "note_title") var noteTitle:String,
                 @ColumnInfo(name = "note_details") var noteDetails:String,
                 @ColumnInfo(name = "note_type") var noteType: Int){

    @PrimaryKey(autoGenerate = true)
    var noteId: Int = 0

    @ColumnInfo(name = "note_pinned")
    var pinned:Int = UNPINNED

/*
    val timeCreated: String
        get() = getCurrentTimeAndDate().first

    val dateCreated: String
        get() = getCurrentTimeAndDate().second


    private fun getCurrentTimeAndDate(): Pair<String,String>{
        val currentDateTime: LocalDateTime = LocalDateTime.now()
        val timeCreated: String = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        val dateCreated: String = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        return Pair(timeCreated,dateCreated)
    }*/

    companion object{
        const val NOTES = 1
        const val ARCHIVED = 2
        const val PINNED = 1
        const val UNPINNED = 0
    }
}