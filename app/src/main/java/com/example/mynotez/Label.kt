package com.example.mynotez

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