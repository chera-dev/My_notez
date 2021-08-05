package com.example.mynotez.fragment.details

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mynotez.*
import com.example.mynotez.Note.Companion.ARCHIVED
import com.example.mynotez.Note.Companion.NOTES
import com.example.mynotez.Note.Companion.PINNED
import com.example.mynotez.Note.Companion.UNPINNED
import com.example.mynotez.databinding.FragmentDetailsBinding
import com.example.mynotez.menu.CreateMenu
import com.example.mynotez.viewmodel.SharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class DetailsFragment : Fragment() {

    private val sharedSharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var editedNote: Note
    private var noteId:Int? = null
    private var listOfLabels = mutableSetOf<Label>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater,container,false)
        val root: View = binding.root
        val titleEditText: EditText = binding.titleTextViewInDetails
        val detailsEditText: EditText = binding.detailsTextViewInDetails

        if(arguments != null) {
            val gotNoteId = requireArguments().getInt("noteId")
            if (gotNoteId != 0)
                noteId = gotNoteId
            if (noteId != null){
                editedNote = sharedSharedViewModel.getNoteById(noteId!!)

                titleEditText.setText(editedNote.noteTitle)
                detailsEditText.setText(editedNote.noteDetails)

                listOfLabels.addAll(sharedSharedViewModel.getLabelsOfThisNote(noteId!!))
            }
            else{
                editedNote = Note("","", NOTES,0)
            }
            val labelId:Int = requireArguments().getInt("labelId")
            if (labelId != 0)
                listOfLabels.add(sharedSharedViewModel.getLabelById(labelId))
            if (listOfLabels.isNotEmpty())
                showLabels()
        }
        binding.textViewDateCreatedInDetails.visibility = View.VISIBLE
        binding.textViewDateCreatedInDetails.text = editedNote.dateCreated
        if (editedNote.pinned == PINNED)
            binding.textViewPinnedInDetails.visibility = View.VISIBLE
        if (editedNote.noteType == ARCHIVED)
            binding.textViewNoteType.visibility = View.VISIBLE

        binding.fabUpdateNotes.setOnClickListener {
            editedNote.noteTitle = titleEditText.text.toString()
            editedNote.noteDetails = detailsEditText.text.toString()
            if(editedNote.noteTitle != "" || editedNote.noteDetails != ""){
                if (noteId != null)
                    sharedSharedViewModel.updateNotes(editedNote)
                else
                    sharedSharedViewModel.addNewNotes(editedNote,listOfLabels)
            }
            else{
                Toast.makeText(requireContext(),"Discarded empty note",Toast.LENGTH_SHORT).show()
            }
            findNavController().popBackStack()
        }
        setHasOptionsMenu(true)
        return root
    }

    private fun showLabels(){
        binding.labelTextViewInDetails.visibility = View.VISIBLE
        binding.chipGroupInDetails.visibility = View.VISIBLE
        binding.chipGroupInDetails.removeAllViews()
        for (i in listOfLabels)
            binding.chipGroupInDetails.addChip(binding.chipGroupInDetails.context,i.labelName)
    }

    private fun ChipGroup.addChip(context: Context, label:String){
        Chip(context).apply {
            text = label
            addView(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val createMenu = CreateMenu(menu)
        createMenu.addMenuItem(
            Menu.NONE, 1, 1, if (editedNote.pinned == PINNED) "unPin" else "pin",
            if (editedNote.pinned == PINNED) R.drawable.ic_baseline_push_unpin_24 else R.drawable.ic_outline_push_pin_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = { itemTitle ->
                if (editedNote.pinned == PINNED) {
                    createMenu.changeIcon(1, R.drawable.ic_outline_push_pin_24)
                    editedNote.pinned = UNPINNED
                    binding.textViewPinnedInDetails.visibility = View.GONE
                    if (noteId != null)
                        sharedSharedViewModel.unpinNote(noteId as Int)
                }
                else{
                    createMenu.changeIcon(1, R.drawable.ic_baseline_push_unpin_24)
                    editedNote.pinned = PINNED
                    binding.textViewPinnedInDetails.visibility = View.VISIBLE
                    if(noteId != null)
                        sharedSharedViewModel.pinNotes(noteId as Int)
                }
            })
        createMenu.addMenuItem(
            Menu.NONE,2,2,"add label", R.drawable.ic_outline_label_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                val listOfAllLabelAvailable:List<Label> = sharedSharedViewModel.getLabels()
                val allLabelName = Array(size = listOfAllLabelAvailable.size){""}
                val selectedLabelList = BooleanArray(listOfAllLabelAvailable.size)

                if (listOfAllLabelAvailable.isEmpty())
                    Toast.makeText(requireContext(),"nothing", Toast.LENGTH_LONG).show()
                else {
                    for (i in listOfAllLabelAvailable.indices) {
                        allLabelName[i] = listOfAllLabelAvailable[i].labelName
                        if (listOfAllLabelAvailable[i] in listOfLabels)
                            selectedLabelList[i] = true
                    }
                }
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("add label")
                builder.setMultiChoiceItems(allLabelName, selectedLabelList){ _, which, isChecked ->
                    selectedLabelList[which] = isChecked
                }
                builder.setPositiveButton("Done"){ _, _ ->
                    for (j in selectedLabelList.indices){
                        if (selectedLabelList[j]){
                            if(noteId != null)
                                sharedSharedViewModel.addLabelWithNote(noteId!!,listOfAllLabelAvailable[j].labelId)
                            else
                                listOfLabels.add(listOfAllLabelAvailable[j])
                        }
                        else{
                            if(noteId != null)
                                sharedSharedViewModel.removeLabelFromNote(listOfAllLabelAvailable[j].labelId,noteId!!)
                            else
                                listOfLabels.remove(listOfAllLabelAvailable[j])
                        }
                    }
                    if(noteId != null)
                        listOfLabels = sharedSharedViewModel.getLabelsOfThisNote(noteId!!)
                    if (listOfLabels.isNotEmpty()) {
                        showLabels()
                    }
                    else {
                        binding.labelTextViewInDetails.visibility = View.GONE
                        binding.chipGroupInDetails.visibility = View.GONE
                    }
                }
                builder.show()
            })
        createMenu.addMenuItem(
            Menu.NONE, 3, 3,
            if (editedNote.noteType != ARCHIVED)"archive" else "unarchive",
            if (editedNote.noteType != ARCHIVED) R.drawable.ic_baseline_archive_24 else R.drawable.ic_baseline_unarchive_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                if (editedNote.noteType != ARCHIVED) {
                    createMenu.changeIcon(3, R.drawable.ic_baseline_unarchive_24)
                    editedNote.noteType = ARCHIVED
                    binding.textViewNoteType.visibility = View.VISIBLE
                    if (noteId != null)
                        sharedSharedViewModel.addNoteToArchive(noteId as Int)
                }
                else{
                    createMenu.changeIcon(3, R.drawable.ic_baseline_archive_24)
                    editedNote.noteType = NOTES
                    binding.textViewNoteType.visibility = View.GONE
                    if(noteId != null)
                        sharedSharedViewModel.removeNoteFromArchive(noteId as Int)
                }
            })
        createMenu.addMenuItem(
            Menu.NONE, 4, 4, "delete", R.drawable.ic_baseline_delete_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
                if(noteId != null)
                    sharedSharedViewModel.deleteNote(noteId as Int)
                findNavController().popBackStack()
            })
        inflater.inflate(R.menu.main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
