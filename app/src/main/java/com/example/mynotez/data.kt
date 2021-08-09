package com.example.mynotez

import com.example.mynotez.enumclass.NoteType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


data class Note(var noteTitle:String, var noteDetails:String, var noteType: NoteType, var noteId: Int, var isPinned:Boolean = false){

    private val currentDateTime: LocalDateTime = LocalDateTime.now()
    val timeCreated: String = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val dateCreated: String = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

    private val labelOfThisNote = mutableSetOf<Int>()

    fun addLabelToThisNote(labelId: Int){
        labelOfThisNote.add(labelId)
    }

    fun getLabelsIdOfThisNote():MutableSet<Int>{
        return labelOfThisNote
    }

    fun removeLabel(labelId: Int){
        labelOfThisNote.remove(labelId)
    }

}
