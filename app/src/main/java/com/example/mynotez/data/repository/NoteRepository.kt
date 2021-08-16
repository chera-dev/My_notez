package com.example.mynotez.data.repository

import androidx.lifecycle.LiveData
import com.example.mynotez.data.Notes
import com.example.mynotez.data.dao.NoteDao
import com.example.mynotez.enumclass.NoteType

class NoteRepository(private val noteDao: NoteDao) {

    val readAllNotes: LiveData<List<Notes>> = noteDao.readAllNotes()

    suspend fun addNote(note: Notes){
        noteDao.addNote(note)
    }

    suspend fun updateNote(note: Notes){
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Notes){
        noteDao.deleteNote(note)
    }

    suspend fun updatePin(noteId:Long, status:Boolean){
        noteDao.changePinStatus(noteId,status)
    }

    suspend fun updateNoteType(noteId: Long,noteType:NoteType){
        noteDao.updateNoteStatus(noteId, noteType)
    }

}