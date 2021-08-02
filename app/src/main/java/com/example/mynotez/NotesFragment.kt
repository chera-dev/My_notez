package com.example.mynotez

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.Note.Companion.ARCHIVED
import com.example.mynotez.Note.Companion.NOTES
import com.example.mynotez.Note.Companion.UNPINNED
import com.example.mynotez.SharedViewModel.Companion.LABEL
import com.example.mynotez.SharedViewModel.Companion.NOTEZ
import com.example.mynotez.databinding.FragmentNotesBinding
import java.util.ArrayList

class NotesFragment : Fragment(),ItemListener {

    private lateinit var binding:FragmentNotesBinding
    private var title = "note"

    private val sharedSharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: NotesAdapter
    private var notesList: List<Note>? = null
    private var noteType:Int = NOTEZ
    private var labelId:Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotesBinding.inflate(inflater,container,false)
        if (arguments!=null) {
            title = requireArguments().getString("title").toString()
            noteType = requireArguments().getInt("type")
            labelId = requireArguments().getInt("labelId")
            //got successfully
        }
        binding.fab.setOnClickListener { view ->
            view?.findNavController()?.navigate(NotesFragmentDirections.actionNavNotesFragToDetailsFragment(null))
        }
        binding.textViewTitleInNotesFragment.text = title

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


        return binding.root
    }

    override fun onClick(position: Int) {
        val data:Note = recyclerAdapter.notesList[position]
        //Toast.makeText(requireContext(),"in notes fragment clicked note ${data.noteTitle}", Toast.LENGTH_SHORT).show()
        view?.findNavController()?.navigate(NotesFragmentDirections.actionNavNotesFragToDetailsFragment(data.noteId.toString()))
    }

    override fun onLongClick(position: Int) {
        val data:Note = recyclerAdapter.notesList[position]
        Toast.makeText(requireContext(),"in notes fragment long clicked note ${data.noteTitle}", Toast.LENGTH_SHORT).show()
        val menuBottomDialog = MenuBottomDialog(requireContext())
        if (title == "note"){
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("archive") {
                Toast.makeText(requireContext(), "archive clicked", Toast.LENGTH_SHORT).show()
                sharedSharedViewModel.addToArchive(data.noteId)
                notesList = sharedSharedViewModel.getNotes()
                recyclerAdapter.changeData(notesList!!)
            })
                .addTextViewItem(MenuBottomDialog.Operation("delete") {
                    Toast.makeText(requireContext(), "delete clicked", Toast.LENGTH_SHORT).show()
                    sharedSharedViewModel.deleteNote(data.noteId)
                    notesList = sharedSharedViewModel.getNotes()
                    recyclerAdapter.changeData(notesList!!)
                })
                .addTextViewItem(MenuBottomDialog.Operation("labels") {
                    Toast.makeText(requireContext(), "label clicked", Toast.LENGTH_SHORT).show()
                    //make alert dialog to show labels available and those which are selected
                        val labelList:List<Label> = sharedSharedViewModel.getLabel()

                        val allLabelName = Array(size = labelList.size){""}
                        val selectedLabelList = BooleanArray(labelList.size)

                        if (labelList.isEmpty())
                            Toast.makeText(requireContext(),"nothing", Toast.LENGTH_LONG).show()

                        else {
                            for (i in 0 until labelList.size) {
                                allLabelName[i] = labelList[i].labelName
                                if (sharedSharedViewModel.isLabelPresentInTheNote(data.noteId, labelList[i].labelId))
                                    selectedLabelList[i] = true
                            }
                        }

                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("add label")
                        builder.setMultiChoiceItems(allLabelName, selectedLabelList){ dialog, which, isChecked ->
                            selectedLabelList[which] = isChecked
                        }
                        builder.setPositiveButton("Done"){ dialogInterface,i ->
                            val list = ArrayList<String>()
                            for (j in selectedLabelList.indices){
                                if (selectedLabelList[j]){
                                    list.add(allLabelName[j])
                                    sharedSharedViewModel.addLabelWithNote(data.noteId,labelList[j].labelId)
                                }
                            }
                            Toast.makeText(requireContext(),"clicked"+ list.toTypedArray()
                                .contentToString(), Toast.LENGTH_LONG).show()
                        }
                        builder.show()
                })
                .addTextViewItem(MenuBottomDialog.Operation(if (data.pinned == UNPINNED) "pin note" else "unPin note") {
                    Toast.makeText(requireContext(), "pinned clicked", Toast.LENGTH_SHORT).show()
                    if (data.pinned == UNPINNED)
                        sharedSharedViewModel.pinNotes(data.noteId)
                    else
                        sharedSharedViewModel.unpinNote(data.noteId)
                    notesList = sharedSharedViewModel.getNotes()
                    recyclerAdapter.changeData(notesList!!)
                }).show()
        }
        else if (title == "Archive"){
            menuBottomDialog.addTextViewItem(MenuBottomDialog.Operation("unarchive") {
                Toast.makeText(requireContext(),"unarchive clicked",Toast.LENGTH_SHORT).show()
                sharedSharedViewModel.removeFromArchive(data.noteId)
                notesList = sharedSharedViewModel.getArchivedNotes()
                recyclerAdapter.changeData(notesList!!)
            })
                .addTextViewItem(MenuBottomDialog.Operation("delete") {
                    Toast.makeText(requireContext(),"delete clicked",Toast.LENGTH_SHORT).show()
                    sharedSharedViewModel.deleteNote(data.noteId)
                    notesList = sharedSharedViewModel.getArchivedNotes()
                    recyclerAdapter.changeData(notesList!!)
                }).show()
        }
    }
}