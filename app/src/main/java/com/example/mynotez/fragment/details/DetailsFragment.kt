package com.example.mynotez.fragment.details

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mynotez.*
import com.example.mynotez.data.Label
import com.example.mynotez.data.NoteViewModel
import com.example.mynotez.data.Notes
import com.example.mynotez.enumclass.NoteType.TYPENOTES
import com.example.mynotez.enumclass.NoteType.TYPEARCHIVED
import com.example.mynotez.databinding.FragmentDetailsBinding
import com.example.mynotez.menu.CreateMenu
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class DetailsFragment : Fragment() {

    private lateinit var mUserViewModel: NoteViewModel



    //*private val sharedSharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //* private lateinit var editedNote: Note
    private lateinit var editedNote: Notes
    private var noteId:Long? = null
    //private var listOfLabels = mutableSetOf<Label>()
    private var listOfLabels = mutableSetOf<String>()

    //@
    private lateinit var titleEditText: EditText
    private lateinit var detailsEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater,container,false)

        mUserViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)


        val root: View = binding.root
        //@
        titleEditText = binding.titleTextViewInDetails
        detailsEditText = binding.detailsTextViewInDetails

        if(arguments != null) {
            //*
            /*val gotNoteId = requireArguments().getInt("noteId")
            if (gotNoteId != 0)
                noteId = gotNoteId*/

            //#
            val gotNote: Notes? = requireArguments().getSerializable("noteToDetails") as Notes?
            if (gotNote != null){
                editedNote = gotNote
                noteId = editedNote.noteId

                titleEditText.setText(editedNote.noteTitle)
                detailsEditText.setText(editedNote.noteDetails)

                listOfLabels.addAll(editedNote.getLabels())
            }
            else{
                editedNote = Notes("","",TYPENOTES)
            }
            //*
            /*if (noteId != null){
                editedNote = sharedSharedViewModel.getNoteById(noteId!!)

                titleEditText.setText(editedNote.noteTitle)
                detailsEditText.setText(editedNote.noteDetails)

                listOfLabels.addAll(sharedSharedViewModel.getLabelsOfThisNote(noteId!!))
            }
            else{
                editedNote = Note("","", NOTES,0)
            }*/

            //val labelId:Int = requireArguments().getInt("labelId")
            val label:String? = requireArguments().getString("label")
            //if (labelId != 0)
            //    listOfLabels.add(sharedSharedViewModel.getLabelById(labelId))
            if (label != null)
                listOfLabels.add(label)
            if (listOfLabels.isNotEmpty())
                showLabels()
        }
        binding.textViewDateCreatedInDetails.visibility = View.VISIBLE
        binding.textViewDateCreatedInDetails.text = editedNote.dateCreated
        if (editedNote.isPinned)
            binding.textViewPinnedInDetails.visibility = View.VISIBLE
        if (editedNote.noteType == TYPEARCHIVED)
            binding.textViewNoteType.visibility = View.VISIBLE

        binding.fabUpdateNotes.setOnClickListener {
            saveNote()
            findNavController().popBackStack()
        }
        setHasOptionsMenu(true)
        return root
    }

    private fun saveNote(){
        editedNote.noteTitle = titleEditText.text.toString()
        editedNote.noteDetails = detailsEditText.text.toString()
        if(editedNote.noteTitle != "" || editedNote.noteDetails != ""){
            //*
            /*if (noteId != null)
                sharedSharedViewModel.updateNotes(editedNote)
            else
                sharedSharedViewModel.addNewNotes(editedNote,listOfLabels)*/

            if (noteId != null)
                mUserViewModel.updateNote(editedNote)
            else
                mUserViewModel.addNote(editedNote)
            //& also send list of label that needs to be added
        }
        else{
            Toast.makeText(requireContext(),"Empty Note Discarded",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLabels(){
        binding.labelTextViewInDetails.visibility = View.VISIBLE
        binding.chipGroupInDetails.visibility = View.VISIBLE
        binding.chipGroupInDetails.removeAllViews()
        for (i in listOfLabels)
            binding.chipGroupInDetails.addChip(binding.chipGroupInDetails.context,i)
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
            Menu.NONE, 1, 1, if (editedNote.isPinned) "unPin" else "pin",
            if (editedNote.isPinned) R.drawable.ic_baseline_push_unpin_24 else R.drawable.ic_outline_push_pin_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = { itemTitle ->
                if (editedNote.isPinned) {
                    createMenu.changeIcon(1, R.drawable.ic_outline_push_pin_24)
                    editedNote.isPinned = false
                    binding.textViewPinnedInDetails.visibility = View.GONE
                    //*if (noteId != null)
                    //*    sharedSharedViewModel.unpinNote(noteId as Int)
                }
                else{
                    createMenu.changeIcon(1, R.drawable.ic_baseline_push_unpin_24)
                    editedNote.isPinned = true
                    binding.textViewPinnedInDetails.visibility = View.VISIBLE
                    //*if(noteId != null)
                    //*    sharedSharedViewModel.pinNotes(noteId as Int)
                }
            })
        ///////
        createMenu.addMenuItem(
            Menu.NONE,2,2,"add label", R.drawable.ic_outline_label_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                //*val listOfAllLabelAvailable:List<Label> = sharedSharedViewModel.getLabels()
                val listOfAllLabelAvailable:List<Label> = mUserViewModel.allLabels.value!!
                val allLabelName = Array(size = listOfAllLabelAvailable.size){""}
                val selectedLabelList = BooleanArray(listOfAllLabelAvailable.size)

                if (listOfAllLabelAvailable.isEmpty())
                    Toast.makeText(requireContext(),"nothing", Toast.LENGTH_LONG).show()
                else {
                    for (i in listOfAllLabelAvailable.indices) {
                        allLabelName[i] = listOfAllLabelAvailable[i].labelName
                        //*if (listOfAllLabelAvailable[i] in listOfLabels)
                        if (listOfAllLabelAvailable[i].labelName in listOfLabels)
                            selectedLabelList[i] = true
                    }

                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("add label")
                    builder.setMultiChoiceItems(allLabelName, selectedLabelList){ _, which, isChecked ->
                        selectedLabelList[which] = isChecked
                    }
                    builder.setPositiveButton("Done"){ _, _ ->
                        for (j in selectedLabelList.indices){
                            if (selectedLabelList[j]){
                                //& no need to add below but can add noteid to labels in add fun of view model
                                if(noteId != null)
                                    mUserViewModel.addLabelWithNote(editedNote,listOfAllLabelAvailable[j])
                                //*sharedSharedViewModel.addLabelWithNote(noteId!!,listOfAllLabelAvailable[j].labelId)
                                else
                                    listOfLabels.add(listOfAllLabelAvailable[j].labelName)
                                //*listOfLabels.add(listOfAllLabelAvailable[j])
                            }
                            else{
                                if(noteId != null)
                                    mUserViewModel.removeLabelFromNote(editedNote,listOfAllLabelAvailable[j])
                                //*sharedSharedViewModel.removeLabelFromNote(listOfAllLabelAvailable[j].labelId,noteId!!)
                                else
                                    listOfLabels.remove(listOfAllLabelAvailable[j].labelName)
                                //*listOfLabels.remove(listOfAllLabelAvailable[j])
                            }
                        }
                        if(noteId != null)
                            listOfLabels = editedNote.getLabels() as MutableSet<String>
                        //*listOfLabels = sharedSharedViewModel.getLabelsOfThisNote(noteId!!)
                        if (listOfLabels.isNotEmpty()) {
                            showLabels()
                        }
                        else {
                            binding.labelTextViewInDetails.visibility = View.GONE
                            binding.chipGroupInDetails.visibility = View.GONE
                        }
                    }
                    builder.show()
                }
            })
        createMenu.addMenuItem(
            Menu.NONE, 3, 3,
            if (editedNote.noteType != TYPEARCHIVED)"archive" else "unarchive",
            if (editedNote.noteType != TYPEARCHIVED) R.drawable.ic_baseline_archive_24 else R.drawable.ic_baseline_unarchive_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                if (editedNote.noteType != TYPEARCHIVED) {
                    createMenu.changeIcon(3, R.drawable.ic_baseline_unarchive_24)
                    editedNote.noteType = TYPEARCHIVED
                    binding.textViewNoteType.visibility = View.VISIBLE
                    //*if (noteId != null)
                    //*    sharedSharedViewModel.addNoteToArchive(noteId as Int)
                }
                else{
                    createMenu.changeIcon(3, R.drawable.ic_baseline_archive_24)
                    editedNote.noteType = TYPENOTES
                    binding.textViewNoteType.visibility = View.GONE
                    //*if(noteId != null)
                    //*    sharedSharedViewModel.removeNoteFromArchive(noteId as Int)
                }
            })
        createMenu.addMenuItem(
            Menu.NONE, 4, 4, "delete", R.drawable.ic_baseline_delete_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                //*if(noteId != null)
                //*    sharedSharedViewModel.deleteNote(noteId as Int)
                //*if(noteId != null)
                //*    mUserViewModel.deleteNote(editedNote)
                //*findNavController().popBackStack()
                //#
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete Note - ${editedNote.noteTitle}?")
                builder.setPositiveButton("Delete"){ _, _ ->
                    Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
                    if(noteId != null)
                        mUserViewModel.deleteNote(editedNote)
                    findNavController().popBackStack()
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                }
                builder.show()

            })
        inflater.inflate(R.menu.main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /*override fun onDestroyView() {
        saveNote()
        super.onDestroyView()
    }*/
}
