package com.example.mynotez

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


data class Note(var noteTitle:String, var noteDetails:String, var noteType: Int, var noteId: Int, var pinned:Int = UNPINNED){

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

    companion object{
        const val NOTES = 1
        const val ARCHIVED = 2
        const val PINNED = 1
        const val UNPINNED = 0
    }
}

data class Label(val labelId:Int, var labelName:String){
    private val notesIdInThisLabel = mutableSetOf<Int>()

    fun addNoteToThisLabel(noteId: Int){
        notesIdInThisLabel.add(noteId)
    }

    fun removeNote(noteId: Int){
        notesIdInThisLabel.remove(noteId)
    }

    fun getNotesIdInThisLabel():MutableSet<Int>{
        return notesIdInThisLabel
    }
}