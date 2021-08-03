package com.example.mynotez

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynotez.Note.Companion.ARCHIVED
import com.example.mynotez.Note.Companion.NOTES
import com.example.mynotez.Note.Companion.PINNED
import com.example.mynotez.Note.Companion.UNPINNED
import com.example.mynotez.databinding.FragmentDetailsBinding
import java.util.*

class DetailsFragment : Fragment() {
    private val sharedSharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    // #cant delete label in a note
    //while updating note in view model update label in notes too..

    private lateinit var editedNote:Note
    private var noteId:Int? = null
    private var listOfLabels = mutableSetOf<Label>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsBinding.inflate(inflater,container,false)
        val root: View = binding.root
        val titleEditText: EditText = binding.titleTextViewInDetails
        val detailsEditText: EditText = binding.detailsTextViewInDetails
        if(arguments != null) {
            val gotNoteId = requireArguments().getInt("noteId")
            if (gotNoteId != 0)
                noteId = gotNoteId
            //unnesesary check
            if (noteId != null){
                editedNote = sharedSharedViewModel.getNote(noteId!!)

                titleEditText.setText(editedNote.noteTitle)
                detailsEditText.setText(editedNote.noteDetails)

                listOfLabels.addAll(sharedSharedViewModel.getLabelsOfThisNote(noteId!!))
            }
            else{
                editedNote = Note("","", NOTES,0)
            }
            val labelId:Int = requireArguments().getInt("labelId")
            if (labelId != 0){
                listOfLabels.add(sharedSharedViewModel.getLabel(labelId))
                Toast.makeText(requireContext(), "label $labelId", Toast.LENGTH_SHORT).show()
            }
            if (listOfLabels.isNotEmpty())
                showLabelRecyclerView()
        }
        binding.textViewDateCreatedInDetails.visibility = View.VISIBLE
        binding.textViewDateCreatedInDetails.text = editedNote.dateCreated
        if (editedNote.pinned == PINNED){
            binding.textViewPinnedInDetails.visibility = View.VISIBLE
            binding.textViewPinnedInDetails.text = "Pinned"
        }
        if (editedNote.noteType == ARCHIVED){
            binding.textViewNoteType.visibility = View.VISIBLE
            binding.textViewNoteType.text = "Archived"
        }

        binding.fabUpdateNotes.setOnClickListener {
            editedNote.noteTitle = titleEditText.text.toString()
            editedNote.noteDetails = detailsEditText.text.toString()
            if(editedNote.noteTitle == "" && editedNote.noteTitle == ""){
                Toast.makeText(requireContext(),"Discarded empty note",Toast.LENGTH_SHORT).show()
            }
            else{
                if (noteId != null) {
                    sharedSharedViewModel.updateNotes(editedNote)
                    Toast.makeText(requireContext(),"update note",Toast.LENGTH_SHORT).show()

                } else {
                    sharedSharedViewModel.addNewNotes(editedNote,listOfLabels)
                    Toast.makeText(requireContext(),"new note",Toast.LENGTH_SHORT).show()
                }
            }
            findNavController().popBackStack()
        }
        setHasOptionsMenu(true)
        return root
    }

    private fun showLabelRecyclerView(){
        binding.labelTextViewInDetails.visibility = View.VISIBLE
        binding.labelRecyclerViewInDetails.visibility = View.VISIBLE
        val adapter = LabelAdapter(listOfLabels)
        binding.labelRecyclerViewInDetails.adapter = adapter
        binding.labelRecyclerViewInDetails.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val createMenu = CreateMenu(menu)
        createMenu.addMenuItem(
            Menu.NONE, 3, 1,
            if (editedNote.noteType != ARCHIVED)"archive" else "unarchive",
            if (editedNote.noteType != ARCHIVED) R.drawable.ic_baseline_archive_24 else R.drawable.ic_baseline_unarchive_24 ,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = { itemTitle ->
                if (editedNote.noteType != ARCHIVED) {
                    Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT)
                        .show()
                    createMenu.changeIcon(3, R.drawable.ic_baseline_unarchive_24)
                    editedNote.noteType = ARCHIVED
                    if (noteId != null)
                        sharedSharedViewModel.addToArchive(noteId as Int)
                }
                else{
                    Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT).show()
                    createMenu.changeIcon(3, R.drawable.ic_baseline_archive_24)
                    editedNote.noteType = ARCHIVED
                    if(noteId != null)
                        sharedSharedViewModel.removeFromArchive(noteId as Int)
                }
            })
        createMenu.addMenuItem(
            Menu.NONE, 1, 2, if (editedNote.pinned == PINNED) "unPin" else "pin",
            if (editedNote.pinned == PINNED) R.drawable.ic_baseline_push_unpin_24 else R.drawable.ic_outline_push_pin_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = { itemTitle ->
                if (editedNote.pinned == PINNED) {
                    Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT)
                        .show()
                    createMenu.changeIcon(1, R.drawable.ic_outline_push_pin_24)
                    editedNote.pinned = UNPINNED
                    if (noteId != null)
                        sharedSharedViewModel.unpinNote(noteId as Int)
                }
                else{
                    Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT).show()
                    createMenu.changeIcon(1, R.drawable.ic_baseline_push_unpin_24)
                    editedNote.pinned = PINNED
                    if(noteId != null)
                        sharedSharedViewModel.pinNotes(noteId as Int)
                }
            })
        createMenu.addMenuItem(
            Menu.NONE, 2, 2, "delete", R.drawable.ic_baseline_delete_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = { itemTitle ->
                Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT).show()
                if(noteId != null)
                    sharedSharedViewModel.deleteNote(noteId as Int)
                findNavController().popBackStack()
            })
        createMenu.addMenuItem(
            Menu.NONE,4,4,"add label",R.drawable.ic_outline_label_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = { itemTitle ->
                Toast.makeText(requireContext(), "$itemTitle clicked", Toast.LENGTH_SHORT).show()

                val listOfAllLabelAvailable:List<Label> = sharedSharedViewModel.getLabels()

                val allLabelName = Array(size = listOfAllLabelAvailable.size){""}
                val selectedLabelList = BooleanArray(listOfAllLabelAvailable.size)

                if (listOfAllLabelAvailable.isEmpty())
                    Toast.makeText(requireContext(),"nothing", Toast.LENGTH_LONG).show()
                else {
                        for (i in 0 until listOfAllLabelAvailable.size) {
                            allLabelName[i] = listOfAllLabelAvailable[i].labelName
                            //error when adding label to new note
                            //when done is clicked already set label is repeated twice
                            if (listOfAllLabelAvailable[i] in listOfLabels)
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
                            if(noteId != null)
                                sharedSharedViewModel.addLabelWithNote(noteId!!,listOfAllLabelAvailable[j].labelId)
                            else
                                listOfLabels.add(listOfAllLabelAvailable[j])
                        }
                        else{
                            //create a function to remove label from note in shared view model
                            if(noteId != null)
                                sharedSharedViewModel.removeLabel(listOfAllLabelAvailable[j].labelId,noteId!!)
                            else
                                listOfLabels.remove(listOfAllLabelAvailable[j])
                        }
                    }
                    //not showing recycler view
                    if(noteId != null)
                        listOfLabels = sharedSharedViewModel.getSetOfLabelsOfThisNote(noteId!!) as MutableSet<Label>
                    showLabelRecyclerView()
                    Toast.makeText(requireContext(),"clicked"+ list.toTypedArray()
                        .contentToString(), Toast.LENGTH_LONG).show()
                }
                builder.show()
            })
        inflater.inflate(R.menu.main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}