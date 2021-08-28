package com.example.mynotez.fragment.note

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ShareCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.*
import com.example.mynotez.data.Content
import com.example.mynotez.data.entities.Label
import com.example.mynotez.data.NoteViewModel
import com.example.mynotez.data.entities.Data
import com.example.mynotez.data.entities.Notes
import com.example.mynotez.data.entities.Title
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
    private var menuSearchView:SearchView? = null

    private var notesList: Content<Notes>? = null
    private var myNotes:List<Notes>? = null
    private var noteFrom: From = NOTES
    private var label: Label? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater,container,false)
        mUserViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)

        if (arguments!=null) {
            title = requireArguments().getString("title").toString()
            val gotNoteFrom = requireArguments().getString("type")
            if(gotNoteFrom != null)
                noteFrom = From.valueOf(gotNoteFrom)
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
        //binding.textViewTitleInNotesFragment.text = title

        recyclerAdapter = NotesAdapter(this)
        getNotes()
        setNotesToRecyclerView()
        setRecyclerView()

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (menuSearchView?.isIconified == false){
                    menuSearchView?.onActionViewCollapsed()
                    onSearchClose()
                }
                else{
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        })
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setNotesToRecyclerView(){
        notesList?.observe(viewLifecycleOwner, {
            myNotes = it
            if (myNotes != null && myNotes!!.isNotEmpty()) {
                recyclerAdapter.changeData(getNotesForRecyclerView(myNotes!!))
                binding.noNotesCardView.visibility = View.GONE
            }
            else {
                recyclerAdapter.changeData(emptyList())
                binding.noNotesCardView.visibility = View.VISIBLE
            }
        })
    }

    private fun getNotesForRecyclerView(data:List<Notes>):List<Data>{
        val dataForRecyclerView = mutableListOf<Data>()
        var noteType = data[0].isPinned
        if (noteType)
            dataForRecyclerView.add(Title("Pinned"))
        else
            dataForRecyclerView.add(Title("Others"))
        for (i in data){
            if (i.isPinned == noteType)
                dataForRecyclerView.add(i)
            else {
                noteType = i.isPinned
                dataForRecyclerView.add(Title("Others"))
                dataForRecyclerView.add(i)
            }
        }
        return dataForRecyclerView
    }

    private fun setRecyclerView(){
        recyclerView = binding.notesRecyclerView
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun getNotes(){
        if(noteFrom == NOTES){
            notesList = mUserViewModel.baseNotes
        }
        else if(noteFrom == ARCHIVED){
            notesList = mUserViewModel.archivedNotes
        }
        else if(noteFrom == LABEL){
            val noteIds = label?.getNoteIds()
            if (noteIds != null && noteIds.isNotEmpty()){
                notesList = mUserViewModel.getNotesOfNoteIds(noteIds)
                if (notesList == null){
                    recyclerAdapter.changeData(emptyList())
                    binding.noNotesCardView.visibility = View.VISIBLE
                }
            }
            else{
                recyclerAdapter.changeData(emptyList())
                binding.noNotesCardView.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(note: Notes) {
        val bundle = Bundle()
        bundle.putSerializable("noteToDetails",note)
        view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
        //val imm:InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //imm.hideSoftInputFromWindow(binding.textViewTitleInNotesFragment.windowToken,0)
    }

    override fun onLongClick(note: Notes) {
        val menuBottomDialog = MenuBottomDialog(requireContext())
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation((if (!note.isPinned) "Pin Note" else "UnPin Note"),
            (if (!note.isPinned) R.drawable.ic_outline_push_pin_24 else R.drawable.ic_baseline_push_unpin_24)) {
            if (!note.isPinned)
                mUserViewModel.changePinStatus(true,note.noteId)
            else
                mUserViewModel.changePinStatus(false,note.noteId)
        }).addTextViewItem(MenuBottomDialog.Operation("Add Labels",R.drawable.ic_outline_label_24) {
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
                val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
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
                            if (labelList[j].labelName == label?.labelName) {
                                label?.addNote(note.noteId)
                            }
                            mUserViewModel.addLabelWithNote(note,labelList[j])
                        } else {
                            if ( labelList[j].labelName == label?.labelName) {
                                label?.removeNote(note.noteId)
                            }
                            mUserViewModel.removeLabelFromNote(note,labelList[j])
                        }
                    }
                    if (label !=null && label?.getNoteIds()?.isEmpty() == true){
                        notesList?.removeObservers(viewLifecycleOwner)
                        recyclerAdapter.changeData(emptyList())
                        binding.noNotesCardView.visibility = View.VISIBLE
                    }
                    else if(label != null){
                        notesList?.removeObservers(viewLifecycleOwner)
                        getNotes()
                        setNotesToRecyclerView()
                    }
                }
                builder.setNegativeButton("Cancel"){ _,_ ->
                }
                builder.show()
            }
        })
        if (noteFrom == ARCHIVED){
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("UnArchive",R.drawable.ic_baseline_unarchive_24) {
                Toast.makeText(requireContext(), "Note Unarchived", Toast.LENGTH_SHORT).show()
                mUserViewModel.changeNoteType(note,NoteType.TYPENOTES)
            })}
        else {
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("Archive",R.drawable.ic_baseline_archive_24) {
                Toast.makeText(requireContext(), "Note Archived", Toast.LENGTH_SHORT).show()
                mUserViewModel.changeNoteType(note,NoteType.TYPEARCHIVED)
            })
        }
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("Make a Copy",R.drawable.ic_baseline_content_copy_24) {
            note.noteId = 0
            mUserViewModel.addNote(note)
        })
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("Delete",R.drawable.ic_baseline_delete_24) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete Note - ${note.noteTitle}?")
            builder.setPositiveButton("Delete"){ _, _ ->
                Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
                mUserViewModel.deleteNote(note)
            }
            builder.setNegativeButton("Cancel"){ _, _ ->
            }
            val dialog = builder.show()
            dialog.window?.setBackgroundDrawableResource(R.color.very_dark_blue)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.white,null))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.white,null))
        })
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("share note",R.drawable.ic_outline_share_24){
            shareText(note)
        }).show()
    }

    private fun shareText(note: Notes) = with(binding) {
        val shareMsg = getString(R.string.share_message, note.noteTitle, note.noteDetails)
        val intent = ShareCompat.IntentBuilder(requireActivity())
            .setType("text/plain").setText(shareMsg).intent
        startActivity(Intent.createChooser(intent, null))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu,menu)
        if (label != null){
            menu.add("Rename label").setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
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
                    // & change the title in tool bar after label renaming
                    //supportActionBar?.title = it.title
                    //binding.textViewTitleInNotesFragment.text = newLabelName
                    imm.hideSoftInputFromWindow(titleEditText.windowToken,0)
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                    imm.hideSoftInputFromWindow(titleEditText.windowToken,0)
                }
                val alertDialog = builder.show()
                titleEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener{
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE ) {
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                            return true
                        }
                        return false
                    }
                })
                true
            }
            menu.add("delete label").setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete label - ${label!!.labelName}?")
                builder.setPositiveButton("Delete"){ _, _ ->
                    Toast.makeText(requireContext(), "Label Deleted", Toast.LENGTH_SHORT).show()
                    myNotes?.let { it1 -> mUserViewModel.deleteLabelFromNotes(it1, label!!) }
                    mUserViewModel.deleteLabel(label!!)
                    findNavController().popBackStack()
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                }
                val dialog = builder.show()
                dialog.window?.setBackgroundDrawableResource(R.color.very_dark_blue)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.white,null))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.white,null))
                true
            }
        }
        val searchItem = menu.findItem(R.id.search_item)
        val searchView = searchItem.actionView as SearchView
        menuSearchView = searchView
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
            onSearchClose()
            false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun onSearchClose(){
        myNotes?.let { recyclerAdapter.changeData(getNotesForRecyclerView(it)) }
        binding.noNotesCardView.visibility = View.GONE
        binding.fab.visibility = View.VISIBLE
    }

    private fun search(text:String?){
        val matchedNotes = mutableListOf<Notes>()
        if (text != null && text != ""){
                myNotes?.forEach { note ->
                    if (note.noteTitle.contains(text, true) || note.noteDetails.contains(text, true)) {
                        matchedNotes.add(note)
                    }
                }
                if (matchedNotes.isEmpty()) {
                    binding.noNotesCardView.visibility = View.VISIBLE
                    recyclerAdapter.changeData(emptyList())
                }
                else {
                    binding.noNotesCardView.visibility = View.GONE
                    recyclerAdapter.changeData(getNotesForRecyclerView(matchedNotes))
                }
        }
        else {
            recyclerAdapter.changeData(emptyList())
            binding.noNotesCardView.visibility = View.GONE
        }
    }

}