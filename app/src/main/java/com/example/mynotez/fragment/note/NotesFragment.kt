package com.example.mynotez.fragment.note

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.*
import com.example.mynotez.data.Content
import com.example.mynotez.data.Label
import com.example.mynotez.data.NoteViewModel
import com.example.mynotez.data.Notes
import com.example.mynotez.databinding.FragmentNotesBinding
import com.example.mynotez.enumclass.From
import com.example.mynotez.enumclass.From.*
import com.example.mynotez.enumclass.NoteType
import com.example.mynotez.menu.MenuBottomDialog

class NotesFragment : Fragment(), ItemListener {

    private lateinit var mUserViewModel: NoteViewModel
    private lateinit var binding:FragmentNotesBinding

    private var title = "note"
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: NotesAdapter

    private var notesList: Content<Notes>? = null
    private var myNotes:List<Notes>? = null
    private var noteType: From = NOTES
    private var label: Label? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater,container,false)
        mUserViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)

        if (arguments!=null) {
            title = requireArguments().getString("title").toString()
            val gotNoteType = requireArguments().getString("type")
            if(gotNoteType != null)
                noteType = From.valueOf(gotNoteType)
            val gotLabel = requireArguments().getSerializable("label")
            if (gotLabel != null) {
                label = gotLabel as Label
            }
        }
        binding.fab.setOnClickListener { view ->
            val bundle = Bundle()
            if (label != null)
                bundle.putSerializable("label",label)
            view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
        }
        binding.textViewTitleInNotesFragment.text = title
        getNotes()

        recyclerAdapter = NotesAdapter(this)
        notesList?.observe(viewLifecycleOwner, {
            recyclerAdapter.changeData(it)
            myNotes = it
            if (myNotes?.isNotEmpty() == true) {
                setRecyclerView()
                binding.noNotesCardView.visibility = View.GONE
            }
            else {
                binding.noNotesCardView.visibility = View.VISIBLE
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setRecyclerView(){
        recyclerView = binding.notesRecyclerView
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun getNotes(){
        if(noteType == NOTES){
            notesList = mUserViewModel.baseNotes
        }
        else if(noteType == ARCHIVED){
            notesList = mUserViewModel.archivedNotes
        }
        else if(noteType == LABEL){
            val noteIds = label?.getNoteIds()
            if (noteIds!=null){
                notesList = mUserViewModel.getNotesOfNoteIds(noteIds)
            }
        }
    }

    override fun onClick(note:Notes) {
        val bundle = Bundle()
        bundle.putSerializable("noteToDetails",note)
        view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
    }

    override fun onLongClick(note:Notes) {
        val menuBottomDialog = MenuBottomDialog(requireContext())
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation(if (!note.isPinned) "pin note" else "unPin note") {
            if (!note.isPinned)
                mUserViewModel.changePinStatus(true,note)
            else
                mUserViewModel.changePinStatus(false,note)
        }).addTextViewItem(MenuBottomDialog.Operation("labels") {
            val labelList: List<Label> = mUserViewModel.allLabels.value!!
            val allLabelName = Array(size = labelList.size) { "" }
            val selectedLabelList = BooleanArray(labelList.size)
            val labelInNotes = note.getLabels()
            if (labelList.isEmpty())
                Toast.makeText(requireContext(), "No Labels Available", Toast.LENGTH_LONG).show()
            else {
                for (i in labelList.indices) {
                    allLabelName[i] = labelList[i].labelName
                    if (labelInNotes.contains( labelList[i].labelName))
                        selectedLabelList[i] = true
                }

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Add Label To This Note")
                builder.setMultiChoiceItems(
                    allLabelName,
                    selectedLabelList
                ) { _, which, isChecked ->
                    selectedLabelList[which] = isChecked
                }
                builder.setPositiveButton("Done") { _, _ ->
                    for (j in selectedLabelList.indices) {
                        if (selectedLabelList[j]) {
                            note.addLabel(labelList[j].labelName)
                            labelList[j].addNote(note.noteId)
                            if (labelList[j].labelName == label?.labelName)
                                label = labelList[j]
                            mUserViewModel.addLabelWithNote(note,labelList[j])
                        } else {
                            note.removeLabel(labelList[j].labelName)
                            labelList[j].removeNote(note.noteId)
                            if (labelList[j].labelName == label?.labelName)
                                label = labelList[j]
                            mUserViewModel.removeLabelFromNote(note,labelList[j])
                        }
                    }
                }
                builder.setNegativeButton("Cancel"){ _,_ ->
                }
                builder.show()
            }
        })
        if (noteType == ARCHIVED){
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("unarchive") {
                Toast.makeText(requireContext(), "Note Unarchived", Toast.LENGTH_SHORT).show()
                mUserViewModel.changeNoteType(note,NoteType.TYPENOTES)
            })}
        else {
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("archive") {
                Toast.makeText(requireContext(), "Note Archived", Toast.LENGTH_SHORT).show()
                mUserViewModel.changeNoteType(note,NoteType.TYPEARCHIVED)
            })
        }
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("delete") {
            Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
            mUserViewModel.deleteNote(note)
        }).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.e("tag","on create options menu")
        inflater.inflate(R.menu.search_menu,menu)
        if (label != null){
            menu.add("Rename label").setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Rename label")
                val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
                val titleEditText = dialogLayout.findViewById<EditText>(R.id.label_title_edit_text)
                titleEditText.setText(label!!.labelName)
                titleEditText.setSelectAllOnFocus(true)
                titleEditText.requestFocus()
                val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Rename Label"){ _, _ ->
                    val newLabelName = titleEditText.text.toString()
                    if (newLabelName != "" && label!!.labelName!=newLabelName) {
                        val noteIds = label!!.getNoteIds()
                        if (noteIds.isNotEmpty()){
                            mUserViewModel.getNotesOfNoteIds(noteIds).observe(viewLifecycleOwner,{
                                for (i in it){
                                    mUserViewModel.changeLabelInNote(i,newLabelName,label!!.labelName)
                                }
                            })
                        }
                        mUserViewModel.renameLabel(label!!.labelName, newLabelName)
                    }
                    binding.textViewTitleInNotesFragment.text = newLabelName
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                    imm.hideSoftInputFromWindow(titleEditText.windowToken,0)
                }
                builder.show()
                true
            }
            menu.add("delete label").setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete label - ${label!!.labelName}?")
                builder.setPositiveButton("Delete"){ _, _ ->
                    Toast.makeText(requireContext(), "Label Deleted", Toast.LENGTH_SHORT).show()
                    val noteIds = label!!.getNoteIds()
                    if (noteIds.isNotEmpty()){
                        mUserViewModel.getNotesOfNoteIds(noteIds).observe(viewLifecycleOwner,{
                            for (i in it){
                                mUserViewModel.removeLabelFromNote(i, label!!)
                            }
                        })
                    }
                    mUserViewModel.deleteLabel(label!!)
                    findNavController().popBackStack()
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                }
                builder.show()
                true
            }
        }
        val searchItem = menu.findItem(R.id.search_item)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Notes"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }
        })
        searchView.setOnSearchClickListener {
            recyclerAdapter.changeData(emptyList())
            binding.noNotesCardView.visibility = View.GONE
            binding.fab.visibility = View.GONE
        }
        searchView.setOnCloseListener {
            myNotes?.let { recyclerAdapter.changeData(it) }
            binding.noNotesCardView.visibility = View.GONE
            binding.fab.visibility = View.VISIBLE
            false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // & change it to access query in dao through view model
    private fun search(text:String?){
        val matchedNotes = mutableListOf<Notes>()
        if (text != null && text != ""){
                myNotes?.forEach { note ->
                    if (note.noteTitle.contains(text, true) || note.noteDetails.contains(text, true)) {
                        matchedNotes.add(note)
                    }
                }
                if (matchedNotes.isEmpty())
                    binding.noNotesCardView.visibility = View.VISIBLE
                else
                    binding.noNotesCardView.visibility = View.GONE
                    //Toast.makeText(requireContext(), "No Notes Matching", Toast.LENGTH_LONG).show()
                recyclerAdapter.changeData(matchedNotes)
        }
        else {
            recyclerAdapter.changeData(emptyList())
            binding.noNotesCardView.visibility = View.GONE
        }
    }

}