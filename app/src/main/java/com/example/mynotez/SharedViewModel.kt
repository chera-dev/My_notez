package com.example.mynotez

import androidx.lifecycle.ViewModel
import com.example.mynotez.Note.Companion.ARCHIVED
import com.example.mynotez.Note.Companion.NOTES
import com.example.mynotez.Note.Companion.PINNED
import com.example.mynotez.Note.Companion.UNPINNED

class SharedViewModel  : ViewModel() {

    private val _noteList = mutableMapOf<Int,Note>()

    private val _labelList = mutableMapOf<Int,Label>()

    private var nextNoteId:Int = 5
    private var nextLabelId:Int = 3

    init {
        _noteList[1] = (Note("Chera","I'm a good girl", NOTES,1))
        _noteList[2] = (Note("Dev","I'm a bad girl", NOTES,2, 1))

        _labelList[1] = (Label(1, "important"))
        _labelList[2] = (Label(2,"not important"))

        _noteList[2]?.addLabelToThisNote(1)
        _labelList[1]?.addNoteToThisLabel(2)

        _noteList[3] = (Note( "archived note one","details of archived note one", ARCHIVED,3))
        _noteList[4] = (Note( "archived note two","details of archived note two", ARCHIVED,4))
    }

    fun getNotes():List<Note>{
        val noteList = mutableListOf<Note>()
        for (i in _noteList.values)
            if (i.noteType == NOTES)
                noteList.add(i)
        //noteList.sortBy { it.pinned }
        noteList.sortByDescending { it.noteId }
        noteList.sortByDescending { it.pinned }
        return noteList
    }

    fun getLabel(labelId: Int):Label{
        return _labelList[labelId]!!
    }

    fun getArchivedNotes():List<Note>{
        val archivedNoteList = mutableListOf<Note>()
        for (i in _noteList.values)
            if (i.noteType == ARCHIVED)
                archivedNoteList.add(i)
        archivedNoteList.sortByDescending { it.noteId }
        archivedNoteList.sortByDescending { it.pinned }
        return archivedNoteList
    }

    fun getLabels():List<Label>{
        val labelList = mutableListOf<Label>()
        for (i in _labelList.values)
            labelList.add(i)
        return labelList
    }

    fun isLabelPresentInTheNote(noteId: Int,labelId: Int):Boolean{
        val labelsInThisNote = _noteList[noteId]?.getLabelsIdOfThisNote()
        if (labelsInThisNote != null) {
            return labelsInThisNote.contains(labelId)
        }
        else return false
    }

    fun addLabel(labelTitle :String):Label{
        val label = Label(nextLabelId,labelTitle)
        _labelList[nextLabelId] = label
        nextLabelId++
        return label
    }

    fun renameLabel(labelId:Int, newLabelName:String){
        _labelList[labelId]?.labelName = newLabelName
    }

    fun deleteLabel(labelId: Int){
        //delete label id in notes
        //for(i in _labelList)
        _labelList.remove(labelId)
    }

    fun removeLabel(labelId: Int,noteId: Int){
        _labelList[labelId]?.removeNote(noteId)
        _noteList[noteId]?.removeLabel(labelId)
    }

    fun addLabelWithNote(noteId: Int,labelId: Int){
        _noteList[noteId]?.addLabelToThisNote(labelId)
        _labelList[labelId]?.addNoteToThisLabel(noteId)
    }

    fun getLabelNamesInTheNote(noteId: Int):List<String>{
        val labelName = mutableListOf<String>()
        for (i in _noteList[noteId]?.getLabelsIdOfThisNote()!!)
            labelName.add(_labelList[i]!!.labelName)
        return labelName
    }

    //fun setChecked()

    fun getLabelsOfThisNote(noteId: Int):List<Label>{
        val label = mutableListOf<Label>()
        for (i in _noteList[noteId]?.getLabelsIdOfThisNote()!!)
            label.add(_labelList[i]!!)
        return label
    }

    fun getSetOfLabelsOfThisNote(noteId: Int):Set<Label>{
        val label = mutableSetOf<Label>()
        for (i in _noteList[noteId]?.getLabelsIdOfThisNote()!!)
            label.add(_labelList[i]!!)
        return label
    }

    fun getNotesOfTheLabel(labelId: Int): List<Note> {
        val notesListOfLabelId = mutableListOf<Note>()
        val label:Label? = _labelList[labelId]
        if (label != null) {
            for (i in label.getNotesIdInThisLabel())
                notesListOfLabelId.add(_noteList[i]!!)
        }
        return notesListOfLabelId
    }

    //add label
    //rename label
    //delete label
    //new fragment to display notes of that label
    //onclick on those displayed notes goes to details fragment

    fun getNote(noteId: Int):Note{
        return _noteList[noteId]!!
    }

    fun pinNotes(noteId: Int){
        _noteList[noteId]?.pinned = PINNED
    }

    fun unpinNote(noteId: Int){
        _noteList[noteId]?.pinned = UNPINNED
    }

    fun addNewNotes(newNote: Note, listOfLabel:MutableSet<Label>){
        newNote.noteId = nextNoteId
        _noteList[nextNoteId++] = newNote
        if (listOfLabel.isNotEmpty())
            for (i in listOfLabel){
                addLabelWithNote(newNote.noteId,i.labelId)
            }
    }

    fun updateNotes(updatedNote:Note){
        _noteList[updatedNote.noteId]?.noteTitle = updatedNote.noteTitle
        _noteList[updatedNote.noteId]?.noteDetails = updatedNote.noteDetails
        _noteList[updatedNote.noteId]?.noteType = updatedNote.noteType
        _noteList[updatedNote.noteId]?.pinned = updatedNote.pinned
    }

    fun addToArchive(noteId: Int){
        _noteList[noteId]?.noteType = ARCHIVED
    }

    fun removeFromArchive(noteId: Int){
        _noteList[noteId]?.noteType = NOTES
    }

    fun deleteNote(noteId: Int){
        _noteList.remove(noteId)
    }

    companion object{
        const val NOTEZ = 1
        const val LABEL = 2
    }

}