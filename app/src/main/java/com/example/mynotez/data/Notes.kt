package com.example.mynotez.data

import androidx.room.*
import com.example.mynotez.enumclass.NoteType
import com.google.gson.Gson
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Entity(tableName = "note_table")
data class Notes(@ColumnInfo(name = "note_title") var noteTitle:String,
                 @ColumnInfo(name = "note_details") var noteDetails:String,
                 @ColumnInfo(name = "note_type") var noteType: NoteType,
                 @ColumnInfo(name = "labels") private val labels:MutableSet<String> = mutableSetOf()):
    Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    var noteId: Long = 0

    @ColumnInfo(name = "note_is_pinned")
    var isPinned:Boolean = false

    fun addLabel(labelName:String){
        labels.add(labelName)
    }

    fun addAllLabels(labelNames:Set<String>){
        labels.addAll(labelNames)
    }

    fun getLabels():Set<String>{
        return labels
    }

    fun removeLabel(labelName: String){
        labels.remove(labelName)
    }

    @Ignore
    private val currentDateTime: LocalDateTime = LocalDateTime.now()

    @ColumnInfo(name = "time_created")
    var timeCreated: String = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    @ColumnInfo(name = "date_created")
    var dateCreated: String = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))


}


class LabelTypeConverter {

    @TypeConverter
    fun listToJson(value: MutableSet<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<String>::class.java).toMutableSet()
}