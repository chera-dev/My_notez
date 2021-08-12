package com.example.mynotez.data

import android.app.Application
import androidx.lifecycle.*
import com.example.mynotez.enumclass.NoteType
import com.example.mynotez.enumclass.NoteType.TYPEARCHIVED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mynotez.enumclass.NoteType.TYPENOTES

class NoteViewModel (application: Application) : AndroidViewModel(application) {
    val allNotes: LiveData<List<Notes>>
    private val noteRepository: NoteRepository
    private val labelRepository: LabelRepository
    val allLabels: LiveData<List<Label>>

    private var nextLabelOrder = 1

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val labelDao = NoteDatabase.getDatabase(application).labelDao()

    // & change it to function and combine both below
    val baseNotes = Content(noteDao.getNotesOfType(TYPENOTES))

    val archivedNotes = Content(noteDao.getNotesOfType(TYPEARCHIVED))

    init {
        noteRepository = NoteRepository(noteDao)
        allNotes = noteRepository.readAllNotes
        //notes = noteRepository.getNotes

        labelRepository = LabelRepository(labelDao)
        allLabels = labelRepository.readAllLabel

    }


    fun addNote(note:Notes){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.addNote(note)
        }
    }

    fun updateNote(note:Notes){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note:Notes){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }

    fun deleteAllNotes(){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteAllNotes()
        }
    }

    /////////
    /*fun getNotesOfNoteIds(noteIds: Set<Long>): List<Notes> {
        //return allNotes.value?.filter { note: Notes -> noteIds.contains(note.noteId) }
        //return noteDao.getNotesOfNoteIds(noteIds)
        return viewModelScope.launch(Dispatchers.IO) {
            noteDao.getNotesOfNoteIds(noteIds)
        }
    }*/

    fun getNotesOfNoteIds(noteIds: Set<Long>): Content<Notes> {
        return Content(noteDao.getNotesOfNoteIds(noteIds))
    }
    //#
    fun addLabelWithNote(note: Notes,label: Label){
        //note.addLabel(label.labelName)
        updateNote(note)
        //label.addNote(note.noteId)
        updateLabel(label)
    }

    //#
    fun removeLabelFromNote(note: Notes,label: Label){
        //note.removeLabel(label.labelName)
        updateNote(note)
        //label.removeNotes(note.noteId)
        updateLabel(label)
    }

    //#
    // & change to update method in dao method
    fun changeNoteType( note:Notes, newNoteType:NoteType){
        note.noteType = newNoteType
        updateNote(note)
    }

    //#
    // & change to update method in dao
    fun renameLabel(label: Label,newLabelName:String){
        label.labelName = newLabelName
        updateLabel(label)
        //& change label names in notes which contains the label
    }

    //# not
    fun getLabelByName(labelName: String):Label?{
        return labelDao.getLabelByName(labelName)
    }

    //not
    fun getNoteById(noteId:Long):Notes?{
        var note:Notes? = null
        viewModelScope.launch(Dispatchers.IO) {
            note = noteDao.getNoteById(noteId)
            note = Notes("sfs","sgfw", TYPENOTES)
        }
        return note
    }


    fun addLabel(labelName:String){
        val label = Label(labelName)
        label.order = nextLabelOrder++
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.addLabel(label)
        }
    }

    fun updateLabel(label: Label){
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.updateLabel(label)
        }
    }

    fun deleteLabel(label: Label){
        viewModelScope.launch(Dispatchers.IO) {
            labelRepository.deleteLabel(label)
        }
    }


    // & change to update in dao method
    fun changePinStatus(status:Boolean,note: Notes){
        note.isPinned = status
        updateNote(note)

    }

}

class Content<T>(liveData: LiveData<List<T>>) : LiveData<List<T>>() {

    private val observer = Observer<List<T>> { list -> value = list }

    init {
        liveData.observeForever(observer)
    }
}
