package com.example.mynotez.fragment.note

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
import com.google.android.material.textfield.TextInputEditText

class NotesFragment : Fragment(), ItemListener {

    private lateinit var mUserViewModel: NoteViewModel


    private lateinit var binding:FragmentNotesBinding
    private var title = "note"

    //*private val sharedSharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: NotesAdapter
    //private var notesList: List<Note>? = null
    //private var notesList: LiveData<List<Notes>>? = null

    ////////////start with list change to live data later
    private var notesList: Content<Notes>? = null
    private var myNotes:List<Notes>? = null
    //#
    private var noteType: From = NOTES
    //private var labelId: Int? = null
    private var label: Label? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotesBinding.inflate(inflater,container,false)

        mUserViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)



        if (arguments!=null) {
            //*
            /*title = requireArguments().getString("title").toString()
            val gotNoteId = requireArguments().getInt("type")
            if(gotNoteId!=0)
                noteType = gotNoteId
            val gotLabelId = requireArguments().getInt("labelId")
            if (gotLabelId!=0)
                label = gotLabelId
             */
            title = requireArguments().getString("title").toString()
            val gotNoteType = requireArguments().getString("type")
            if(gotNoteType != null)
                noteType = From.valueOf(gotNoteType)
            val gotLabel = requireArguments().getSerializable("label")
            if (gotLabel != null) {
                label = gotLabel as Label
                //Toast.makeText(requireContext(),"$label",Toast.LENGTH_LONG).show()
            }
        }

        binding.fab.setOnClickListener { view ->
            val bundle = Bundle()
            if (label != null)
                bundle.putString("label",label?.labelName)
            //bundle.putInt("labelId",label!!)
            view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
        }
        binding.textViewTitleInNotesFragment.text = title

        getNotes()

        recyclerAdapter = NotesAdapter(this)
        notesList?.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.changeData(it)
            myNotes = it

            if (myNotes?.isNotEmpty() == true) {
                setRecyclerView()
                binding.searchBar.visibility = View.VISIBLE
                binding.noNotesCardView.visibility = View.GONE
                performSearch()
            }
            else {
                binding.searchBar.visibility = View.GONE
                binding.noNotesCardView.visibility = View.VISIBLE
            }
        })

        if (label != null)
            setHasOptionsMenu(true)
        return binding.root
    }

    private fun setRecyclerView(){
        recyclerView = binding.notesRecyclerView
        //*recyclerAdapter = NotesAdapter(notesList!!, this, sharedSharedViewModel)

        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun performSearch(){
        val searchBar = binding.searchBar
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }
        })
        val closeBtnId = searchBar.context.resources.getIdentifier("android:id/search_close_btn",null,null)
        val closeBtn:ImageView = searchBar.findViewById(closeBtnId)
        closeBtn.setOnClickListener {
            searchBar.setQuery("",true)
            searchBar.clearFocus()
        }
    }

    // & change it to access query in dao through view model
    private fun search(text:String?){
        val matchedNotes = mutableListOf<Notes>()
        text?.let {
            myNotes?.forEach { note->
                if (note.noteTitle.contains(text,true)||note.noteDetails.contains(text,true)){
                    matchedNotes.add(note)
                }
            }
            if (matchedNotes.isEmpty())
                Toast.makeText(requireContext(),"No Notes Matching",Toast.LENGTH_LONG).show()
            recyclerAdapter.changeData(matchedNotes)
        }
    }

    private fun getNotes(){
        // *
        /*if (noteType == NOTEZ){
            if (title == "note") {
                notesList = sharedSharedViewModel.getNotes()
            } else if (title == "Archive") {
                notesList = sharedSharedViewModel.getArchivedNotes()
                binding.fab.visibility = View.GONE
            }
        }
        else if(noteType == LABEL){
            val list = label?.let { sharedSharedViewModel.getNotesOfTheLabel(it) }
            if (list != null)
                notesList = list
        }*/

        if(noteType == NOTES){
            notesList = mUserViewModel.baseNotes
        }
        else if(noteType == ARCHIVED){
            notesList = mUserViewModel.archivedNotes
        }
        else if(noteType == LABEL){
            val noteIds = label?.getNotes()
            Toast.makeText(requireContext(),"$noteIds",Toast.LENGTH_LONG).show()
            if (noteIds!=null){
                notesList = mUserViewModel.getNotesOfNoteIds(noteIds)
            }
            //notesList = label?.getNotes()?.let { mUserViewModel.getNotesOfNoteIds(it) }
        }
    }

    override fun onClick(position: Int) {
        //*val data: Note = recyclerAdapter.notesList[position]
        val data: Notes = recyclerAdapter.notesList[position]
        val bundle = Bundle()
        //bundle.putInt("noteId",data.noteId)
        bundle.putSerializable("noteToDetails",data)
        view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
    }

    override fun onLongClick(position: Int) {
        //*val data: Note = recyclerAdapter.notesList[position]
        val data: Notes = recyclerAdapter.notesList[position]
        val menuBottomDialog = MenuBottomDialog(requireContext())
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation(if (!data.isPinned) "pin note" else "unPin note") {
            if (!data.isPinned)
                mUserViewModel.changePinStatus(true,data)
                //*sharedSharedViewModel.pinNotes(data.noteId)
            else
                mUserViewModel.changePinStatus(false,data)
                //*sharedSharedViewModel.unpinNote(data.noteId)
            //^getNotes()
            //^recyclerAdapter.changeData(notesList!!)
        }).addTextViewItem(MenuBottomDialog.Operation("labels") {
            //*val labelList: List<Label> = sharedSharedViewModel.getLabels()
            val labelList: List<Label> = mUserViewModel.allLabels.value!!
            val allLabelName = Array(size = labelList.size) { "" }
            val selectedLabelList = BooleanArray(labelList.size)
            val labelInNotes = data.getLabels()
            if (labelList.isEmpty())
                Toast.makeText(requireContext(), "No Labels Available", Toast.LENGTH_LONG).show()
            else {
                for (i in labelList.indices) {
                    allLabelName[i] = labelList[i].labelName
                    //*if (sharedSharedViewModel.isLabelPresentInTheNote(data.noteId, labelList[i].labelId))
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
                            data.addLabel(labelList[j].labelName)
                            labelList[j].addNote(data.noteId)
                            if (labelList[j].labelName == label?.labelName)
                                label = labelList[j]
                            mUserViewModel.addLabelWithNote(data,labelList[j])
                            //*sharedSharedViewModel.addLabelWithNote(data.noteId, labelList[j].labelId)
                        } else {
                            data.removeLabel(labelList[j].labelName)
                            labelList[j].removeNotes(data.noteId)
                            if (labelList[j].labelName == label?.labelName)
                                label = labelList[j]
                            mUserViewModel.removeLabelFromNote(data,labelList[j])
                            //*sharedSharedViewModel.removeLabelFromNote(labelList[j].labelId, data.noteId)
                        }
                    }
                    //^getNotes()
                    //^recyclerAdapter.changeData(notesList!!)
                }
                builder.setNegativeButton("Cancel"){ _,_ ->
                }
                builder.show()
            }
        })
        //*if (title == "Archive"){
        if (noteType == ARCHIVED){
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("unarchive") {
                Toast.makeText(requireContext(), "Note Unarchived", Toast.LENGTH_SHORT).show()
                //*sharedSharedViewModel.removeNoteFromArchive(data.noteId)
                mUserViewModel.changeNoteType(data,NoteType.TYPENOTES)
                //^getNotes()
                //^recyclerAdapter.changeData(notesList!!)
            })}
        else {
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("archive") {
                Toast.makeText(requireContext(), "Note Archived", Toast.LENGTH_SHORT).show()
                //*sharedSharedViewModel.addNoteToArchive(data.noteId)
                mUserViewModel.changeNoteType(data,NoteType.TYPEARCHIVED)
                //^getNotes()
                //^recyclerAdapter.changeData(notesList!!)
            })
        }
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("delete") {
            Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
            //sharedSharedViewModel.deleteNote(data.noteId)
            mUserViewModel.deleteNote(data)
            //^getNotes()
            //^recyclerAdapter.changeData(notesList!!)
        }).show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (label != null){
            menu.add("Rename label").setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(requireContext())
                //*val label = sharedSharedViewModel.getLabelById(label!!)
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
                    val title = titleEditText.text.toString()
                    if (title != "" && label!!.labelName!=title)
                        mUserViewModel.renameLabel(label!!,title)
                        //*sharedSharedViewModel.renameLabel(this.label!!,title)
                    //*notesList = sharedSharedViewModel.getNotesOfTheLabel(this.label!!)


                    //notesList = label?.getNotes()?.let { mUserViewModel.getNotesOfNoteIds(it) }
                    //^recyclerAdapter.changeData(notesList!!)
                    binding.textViewTitleInNotesFragment.text = title
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                    imm.hideSoftInputFromWindow(titleEditText.windowToken,0)
                }
                builder.show()
                true
            }
            menu.add("delete label").setOnMenuItemClickListener { itemTitle ->
                Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(requireContext())
                //*val label = sharedSharedViewModel.getLabelById(label!!)
                builder.setTitle("Delete label - ${label!!.labelName}?")
                builder.setPositiveButton("Delete"){ _, _ ->
                    mUserViewModel.deleteLabel(label!!)
                    //*sharedSharedViewModel.deleteLabel(this.label!!)
                    findNavController().popBackStack()
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                }
                builder.show()
                true
            }
        }

        inflater.inflate(R.menu.main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}