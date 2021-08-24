package com.example.mynotez.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotez.data.entities.Notes
import com.example.mynotez.enumclass.NoteType

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: Notes)

    @Update
    suspend fun updateNote(note: Notes)

    @Delete
    suspend fun deleteNote(note: Notes)

    @Query("SELECT * FROM note_table ORDER BY note_id DESC")
    fun readAllNotes(): LiveData<List<Notes>>

    @Query("SELECT * FROM note_table WHERE note_type=:noteType ORDER BY note_is_pinned DESC, note_id DESC")
    fun getNotesOfType(noteType:NoteType):LiveData<List<Notes>>

    @Query("SELECT * FROM note_table WHERE note_id IN (:noteIds) ORDER BY note_is_pinned DESC, note_id DESC")
    fun getNotesOfNoteIds(noteIds: Set<Long>):LiveData<List<Notes>>

    @Query("UPDATE note_table SET note_is_pinned =:status WHERE note_id =:noteId")
    suspend fun changePinStatus(noteId:Long, status:Boolean)

    @Query("UPDATE note_table SET note_type =:noteType WHERE note_id =:noteId")
    suspend fun updateNoteStatus(noteId: Long,noteType: NoteType)

}