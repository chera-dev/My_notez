package com.example.mynotez.fragment.details

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mynotez.*
import com.example.mynotez.data.entities.Label
import com.example.mynotez.data.NoteViewModel
import com.example.mynotez.data.entities.Notes
import com.example.mynotez.enumclass.NoteType.TYPENOTES
import com.example.mynotez.enumclass.NoteType.TYPEARCHIVED
import com.example.mynotez.databinding.FragmentDetailsBinding
import com.example.mynotez.menu.CreateMenu
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.lang.Exception
import java.util.*

class DetailsFragment : Fragment() {

    private lateinit var mUserViewModel: NoteViewModel
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private var noteId:Long? = null
    private lateinit var editedNote: Notes
    private var labelFromNotesFragment: Label? = null
    private var listOfLabels = mutableSetOf<String>()

    private lateinit var titleEditText: EditText
    private lateinit var detailsEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater,container,false)
        mUserViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)

        val root: View = binding.root
        titleEditText = binding.titleTextViewInDetails
        detailsEditText = binding.detailsTextViewInDetails

        if (savedInstanceState != null){
            val savedNote = savedInstanceState.getSerializable("myNote") as Notes?
            if (savedNote != null)
                editedNote = savedNote
        }
        else {
            if (arguments != null) {
                val gotNote: Notes? = requireArguments().getSerializable("noteToDetails") as Notes?
                if (gotNote != null) {
                    editedNote = gotNote
                    noteId = editedNote.noteId

                    titleEditText.setText(editedNote.noteTitle)
                    detailsEditText.setText(editedNote.noteDetails)

                    listOfLabels.addAll(editedNote.getLabels())
                } else
                    createEmptyNote()
                val gotLabel = requireArguments().getSerializable("label")
                if (gotLabel != null) {
                    labelFromNotesFragment = gotLabel as Label
                    labelFromNotesFragment?.labelName?.let { listOfLabels.add(it) }
                }
                if (listOfLabels.isNotEmpty())
                    showLabels()
            } else
                createEmptyNote()
        }
        binding.textViewDateCreatedInDetails.visibility = View.VISIBLE
        binding.textViewDateCreatedInDetails.text = editedNote.dateCreated
        if (editedNote.isPinned)
            binding.textViewPinnedInDetails.visibility = View.VISIBLE
        if (editedNote.noteType == TYPEARCHIVED)
            binding.textViewNoteType.visibility = View.VISIBLE
        if (editedNote.noteTitle == "" && editedNote.noteDetails == "")
            binding.textViewDateCreatedInDetails.visibility = View.GONE
        setHasOptionsMenu(true)
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveNote()
        outState.putSerializable("myNote",editedNote)
        super.onSaveInstanceState(outState)
    }

    private fun createEmptyNote(){
        editedNote = Notes("","",TYPENOTES)
        mUserViewModel.addNote(Notes("","",TYPENOTES))
        mUserViewModel.allNotes.observe(viewLifecycleOwner,{
            if (it.isNotEmpty()){
                val recentNote = it.first()
                if (recentNote.noteDetails == "") {
                    editedNote = recentNote
                    noteId = recentNote.noteId
                    if (labelFromNotesFragment != null)
                        mUserViewModel.addLabelWithNote(editedNote, labelFromNotesFragment!!)
                    mUserViewModel.allLabels.removeObservers(viewLifecycleOwner)
                    arguments = null
                }
            }
        })

        titleEditText.requestFocus()
        val imm:InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun saveNote(){
        editedNote.noteTitle = titleEditText.text.toString()
        editedNote.noteDetails = detailsEditText.text.toString()
        editedNote.addAllLabels(listOfLabels)
        if(editedNote.noteTitle != "" || editedNote.noteDetails != ""){
            mUserViewModel.updateNote(editedNote)
        }
        else{
            Toast.makeText(requireContext(),"Empty Note Discarded",Toast.LENGTH_SHORT).show()
            mUserViewModel.deleteNote(editedNote)
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
        createMenu.addMenuItem(Menu.NONE, 1, 1, if (editedNote.isPinned) "UnPin" else "Pin",
            if (editedNote.isPinned) R.drawable.ic_baseline_push_unpin_24 else R.drawable.ic_outline_push_pin_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                if (editedNote.isPinned) {
                    createMenu.changeIcon(1, R.drawable.ic_outline_push_pin_24)
                    editedNote.isPinned = false
                    binding.textViewPinnedInDetails.visibility = View.GONE
                }
                else{
                    createMenu.changeIcon(1, R.drawable.ic_baseline_push_unpin_24)
                    editedNote.isPinned = true
                    binding.textViewPinnedInDetails.visibility = View.VISIBLE
                }
            })
        createMenu.addMenuItem(Menu.NONE,2,2,"Add Label", R.drawable.ic_outline_label_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                val listOfAllLabelAvailable:List<Label> = mUserViewModel.allLabels.value!!
                val allLabelName = Array(size = listOfAllLabelAvailable.size){""}
                val selectedLabelList = BooleanArray(listOfAllLabelAvailable.size)

                if (listOfAllLabelAvailable.isEmpty())
                    Toast.makeText(requireContext(),"No Labels", Toast.LENGTH_LONG).show()
                else {
                    for (i in listOfAllLabelAvailable.indices) {
                        allLabelName[i] = listOfAllLabelAvailable[i].labelName
                        if (listOfAllLabelAvailable[i].labelName in listOfLabels)
                            selectedLabelList[i] = true
                    }
                    val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
                    builder.setTitle("add label")
                    builder.setMultiChoiceItems(allLabelName, selectedLabelList){ _, which, isChecked ->
                        selectedLabelList[which] = isChecked
                    }
                    builder.setPositiveButton("Done"){ _, _ ->
                        for (j in selectedLabelList.indices){
                            if (selectedLabelList[j]){
                                mUserViewModel.addLabelWithNote(editedNote,listOfAllLabelAvailable[j])
                                listOfLabels.add(listOfAllLabelAvailable[j].labelName)
                            }
                            else{
                                mUserViewModel.removeLabelFromNote(editedNote,listOfAllLabelAvailable[j])
                                listOfLabels.remove(listOfAllLabelAvailable[j].labelName)
                            }
                        }
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
        createMenu.addMenuItem(Menu.NONE,3,3,"Speech to text",R.drawable.ic_baseline_keyboard_voice_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS,onclick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                try {
                    startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT)
                }
                catch (e: Exception){
                    Toast.makeText(requireContext(),e.message,Toast.LENGTH_LONG).show()
                }
            })
        createMenu.addMenuItem(Menu.NONE, 4, 4, if (editedNote.noteType != TYPEARCHIVED)"Archive" else "UnArchive",
            if (editedNote.noteType != TYPEARCHIVED) R.drawable.ic_baseline_archive_24 else R.drawable.ic_baseline_unarchive_24,
            MenuItem.SHOW_AS_ACTION_ALWAYS, onclick = {
                if (editedNote.noteType != TYPEARCHIVED) {
                    createMenu.changeIcon(4, R.drawable.ic_baseline_unarchive_24)
                    editedNote.noteType = TYPEARCHIVED
                    binding.textViewNoteType.visibility = View.VISIBLE
                }
                else{
                    createMenu.changeIcon(4, R.drawable.ic_baseline_archive_24)
                    editedNote.noteType = TYPENOTES
                    binding.textViewNoteType.visibility = View.GONE
                }
            })
        createMenu.addMenuItem(Menu.NONE, 5, 5, "Delete", R.drawable.ic_baseline_delete_24,
            MenuItem.SHOW_AS_ACTION_NEVER, onclick = {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete Note - ${editedNote.noteTitle}?")
                builder.setPositiveButton("Delete"){ _, _ ->
                    Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
                    mUserViewModel.deleteNote(editedNote)
                    findNavController().popBackStack()
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                }
                val dialog = builder.show()
                dialog.window?.setBackgroundDrawableResource(R.color.very_dark_blue)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.white,null))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.white,null))
            })
        createMenu.addMenuItem(Menu.NONE,6,6,"Make a Copy",R.drawable.ic_baseline_content_copy_24,
            MenuItem.SHOW_AS_ACTION_NEVER,onclick = {
                Toast.makeText(requireContext(),"Note Copied",Toast.LENGTH_SHORT).show()
                saveNote()
                val note = editedNote
                note.noteId = 0
                mUserViewModel.addNote(note)
            })
        createMenu.addMenuItem(Menu.NONE,7,7,"Share",R.drawable.ic_outline_share_24,
            MenuItem.SHOW_AS_ACTION_NEVER,onclick = {
                shareText()
            })
        inflater.inflate(R.menu.main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun shareText() = with(binding) {
        saveNote()
        val shareMsg = getString(R.string.share_message, editedNote.noteTitle, editedNote.noteDetails)
        val intent = ShareCompat.IntentBuilder(requireActivity())
            .setType("text/plain").setText(shareMsg).intent
        startActivity(Intent.createChooser(intent, null))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT){
            if (resultCode == Activity.RESULT_OK && data != null){
                val result:ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                editedNote.noteDetails = binding.detailsTextViewInDetails.text.toString()
                if (editedNote.noteDetails != "")
                    editedNote.noteDetails += ("\n" + result[0])
                else
                    editedNote.noteDetails = result[0]
                binding.detailsTextViewInDetails.setText(editedNote.noteDetails)
                binding.detailsTextViewInDetails.setSelection(editedNote.noteDetails.length)
            }
        }
    }

    override fun onDestroyView() {
        saveNote()
        super.onDestroyView()
    }

    companion object{
        const val REQUEST_CODE_SPEECH_INPUT = 1}
}
