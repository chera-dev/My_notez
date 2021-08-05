package com.example.mynotez.fragment.note

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.*
import com.example.mynotez.Note.Companion.UNPINNED
import com.example.mynotez.viewmodel.SharedViewModel.Companion.LABEL
import com.example.mynotez.viewmodel.SharedViewModel.Companion.NOTEZ
import com.example.mynotez.databinding.FragmentNotesBinding
import com.example.mynotez.menu.MenuBottomDialog
import com.example.mynotez.viewmodel.SharedViewModel
import java.util.ArrayList

class NotesFragment : Fragment(), ItemListener {

    private lateinit var binding:FragmentNotesBinding
    private var title = "note"

    private val sharedSharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: NotesAdapter
    private var notesList: List<Note>? = null
    private var noteType: Int = NOTEZ
    private var labelId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotesBinding.inflate(inflater,container,false)
        if (arguments!=null) {
            title = requireArguments().getString("title").toString()
            val gotNoteId = requireArguments().getInt("type")
            if(gotNoteId!=0)
                noteType = gotNoteId
            val gotLabelId = requireArguments().getInt("labelId")
            if (gotLabelId!=0)
                labelId = gotLabelId
        }
        binding.fab.setOnClickListener { view ->
            val bundle = Bundle()
            if (labelId!=null)
                bundle.putInt("labelId",labelId!!)
            view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
        }
        binding.textViewTitleInNotesFragment.text = title

        getNotes()

        if (notesList?.isNotEmpty() == true) {
            recyclerView = binding.notesRecyclerView
            recyclerAdapter = NotesAdapter(notesList!!, this, sharedSharedViewModel)
            recyclerView.adapter = recyclerAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        else {
            Toast.makeText(requireContext(), "no no no notes", Toast.LENGTH_LONG).show()
            //make a text view and image view of gone type to show no notes available
        }
        if (labelId != null)
            setHasOptionsMenu(true)
        return binding.root
    }

    private fun getNotes(){
        if (noteType == NOTEZ){
            if (title == "note") {
                notesList = sharedSharedViewModel.getNotes()
            } else if (title == "Archive") {
                notesList = sharedSharedViewModel.getArchivedNotes()
                binding.fab.visibility = View.GONE
            }
        }
        else if(noteType == LABEL){
            val list = labelId?.let { sharedSharedViewModel.getNotesOfTheLabel(it) }
            if (list != null)
                notesList = list
        }
    }

    override fun onClick(position: Int) {
        val data: Note = recyclerAdapter.notesList[position]
        val bundle = Bundle()
        bundle.putInt("noteId",data.noteId)
        view?.findNavController()?.navigate(R.id.action_nav_notes_frag_to_detailsFragment,bundle)
    }

    override fun onLongClick(position: Int) {
        val data: Note = recyclerAdapter.notesList[position]
        val menuBottomDialog = MenuBottomDialog(requireContext())
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation(if (data.pinned == UNPINNED) "pin note" else "unPin note") {
            if (data.pinned == UNPINNED)
                sharedSharedViewModel.pinNotes(data.noteId)
            else
                sharedSharedViewModel.unpinNote(data.noteId)
            getNotes()
            recyclerAdapter.changeData(notesList!!)
        }).addTextViewItem(MenuBottomDialog.Operation("labels") {
            val labelList: List<Label> = sharedSharedViewModel.getLabels()
            val allLabelName = Array(size = labelList.size) { "" }
            val selectedLabelList = BooleanArray(labelList.size)
            if (labelList.isEmpty())
                Toast.makeText(requireContext(), "No Labels Available", Toast.LENGTH_LONG).show()
            else {
                for (i in labelList.indices) {
                    allLabelName[i] = labelList[i].labelName
                    if (sharedSharedViewModel.isLabelPresentInTheNote(
                            ////////////////
                            data.noteId,
                            labelList[i].labelId
                        )
                    )
                        selectedLabelList[i] = true
                }
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
                        sharedSharedViewModel.addLabelWithNote(
                            data.noteId,
                            labelList[j].labelId
                        )
                    } else {
                        sharedSharedViewModel.removeLabelFromNote(
                            labelList[j].labelId,
                            data.noteId
                        )
                    }
                }
                getNotes()
                recyclerAdapter.changeData(notesList!!)
            }
            builder.setNegativeButton("Cancel"){ _,_ ->
            }
            builder.show()
        })
        if (title == "Archive"){
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("unarchive") {
                Toast.makeText(requireContext(), "Note Unarchived", Toast.LENGTH_SHORT).show()
                sharedSharedViewModel.removeNoteFromArchive(data.noteId)
                getNotes()
                recyclerAdapter.changeData(notesList!!)
            })}
        else {
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("archive") {
                Toast.makeText(requireContext(), "Note Archived", Toast.LENGTH_SHORT).show()
                sharedSharedViewModel.addNoteToArchive(data.noteId)
                notesList = sharedSharedViewModel.getNotes()
                recyclerAdapter.changeData(notesList!!)
            })
        }
        menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("delete") {
            Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
            sharedSharedViewModel.deleteNote(data.noteId)
            getNotes()
            recyclerAdapter.changeData(notesList!!)
        }).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (labelId != null){
            menu.add("Rename label").setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(requireContext())
                val label = sharedSharedViewModel.getLabelById(labelId!!)
                builder.setTitle("Rename label - ${label.labelName}")
                val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
                val titleEditText = dialogLayout.findViewById<EditText>(R.id.label_title_edit_text)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Rename Label"){ _, _ ->
                    val title = titleEditText.text.toString()
                    if (title != "")
                        sharedSharedViewModel.renameLabel(labelId!!,title)
                    notesList = sharedSharedViewModel.getNotesOfTheLabel(labelId!!)
                    recyclerAdapter.changeData(notesList!!)
                    binding.textViewTitleInNotesFragment.text = title
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                }
                builder.show()
                true
            }
            menu.add("delete label").setOnMenuItemClickListener { itemTitle ->
                Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(requireContext())
                val label = sharedSharedViewModel.getLabelById(labelId!!)
                builder.setTitle("Delete label - ${label.labelName}")
                builder.setPositiveButton("Delete"){ _, _ ->
                    sharedSharedViewModel.deleteLabel(labelId!!)
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