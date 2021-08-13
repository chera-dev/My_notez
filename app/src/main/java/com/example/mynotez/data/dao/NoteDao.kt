package com.example.mynotez.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotez.data.Notes
import com.example.mynotez.enumclass.NoteType

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: Notes)

    @Update
    suspend fun updateNote(note: Notes)

    @Delete
    suspend fun deleteNote(note: Notes)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM note_table ORDER BY note_id DESC")
    fun readAllNotes(): LiveData<List<Notes>>

    //okay
    @Query("SELECT * FROM note_table WHERE note_type=:noteType")
    fun getNotesOfType(noteType:NoteType):LiveData<List<Notes>>

    // okay
    @Query("SELECT * FROM note_table WHERE note_id IN (:noteIds)")
    fun getNotesOfNoteIds(noteIds: Set<Long>):LiveData<List<Notes>>

    //#
    @Query("SELECT * FROM note_table WHERE note_id = :noteId LIMIT 1")
    fun getNoteById(noteId:Long): LiveData<Notes>

    //not
    //@Query("SELECT * FROM note_table WHERE :label IN (labels)")
    //suspend fun getBaseNotesByLabel(label: String): LiveData<List<Notes>>


}