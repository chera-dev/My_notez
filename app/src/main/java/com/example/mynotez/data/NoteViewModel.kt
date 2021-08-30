package com.example.mynotez.data

import android.app.Application
import androidx.lifecycle.*
import com.example.mynotez.data.database.NoteDatabase
import com.example.mynotez.data.entities.Label
import com.example.mynotez.data.entities.Notes
import com.example.mynotez.data.repository.LabelRepository
import com.example.mynotez.data.repository.NoteRepository
import com.example.mynotez.enumclass.NoteType
import com.example.mynotez.enumclass.NoteType.TYPEARCHIVED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mynotez.enumclass.NoteType.TYPENOTES

class NoteViewModel (application: Application) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository
    private val labelRepository: LabelRepository
    val allNotes: LiveData<List<Notes>>
    val allLabels: LiveData<List<Label>>

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val labelDao = NoteDatabase.getDatabase(application).labelDao()

    val baseNotes = Content(noteDao.getNotesOfType(TYPENOTES))
    val archivedNotes = Content(noteDao.getNotesOfType(TYPEARCHIVED))

    init {
        noteRepository = NoteRepository(noteDao)
        allNotes = noteRepository.readAllNotes

        labelRepository = LabelRepository(labelDao)
        allLabels = labelRepository.readAllLabel
    }

    fun addNote(note: Notes){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.addNote(note)
        }
    }

    fun updateNote(note: Notes){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Notes){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }

    fun getNotesOfNoteIds(noteIds: Set<Long>): Content<Notes> {
        return Content(noteDao.getNotesOfNoteIds(noteIds))
    }

    fun addLabelWithNote(note: Notes, label: Label){
        note.addLabel(label.labelName)
        updateNote(note)
        label.addNote(note.noteId)
        updateLabel(label)
    }

    fun removeLabelFromNote(note: Notes, label: Label){
        note.removeLabel(label.labelName)
        updateNote(note)
        label.removeNote(note.noteId)
        updateLabel(label)
    }

    fun deleteLabelFromNotes(notes: List<Notes>,label:Label){
        for (i in notes) {
            removeLabelFromNote(i,label)
        }
    }

    fun changeLabelInNote(note: Notes, newLabelName: String, oldLabelName: String){
        note.removeLabel(oldLabelName)
        note.addLabel(newLabelName)
        updateNote(note)
    }

    fun changeNoteType(note: Notes, newNoteType:NoteType){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNoteType(note.noteId,newNoteType)
        }
    }

    fun changePinStatus(status:Boolean,noteId:Long){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updatePin(noteId,status)
        }
    }

    fun addLabel(labelName:String){
        val label = Label(labelName)
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.addLabel(label)
        }
    }

    private fun updateLabel(label: Label){
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.updateLabel(label)
        }
    }

    fun deleteLabel(label: Label){
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.deleteLabel(label)
        }
    }

    fun renameLabel(oldLabelName:String ,newLabelName:String){
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.renameLabel(oldLabelName, newLabelName)
        }
    }

}

class Content<T>(liveData: LiveData<List<T>>) : LiveData<List<T>>() {

    private val observer = Observer<List<T>> { list -> value = list }

    init {
        liveData.observeForever(observer)
    }
}