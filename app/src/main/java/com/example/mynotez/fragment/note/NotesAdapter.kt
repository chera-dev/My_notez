package com.example.mynotez.fragment.note

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.*
import com.example.mynotez.data.entities.Data
import com.example.mynotez.data.entities.Notes
import com.example.mynotez.data.entities.Title
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class NotesAdapter ( private val itemListener: ItemListener?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var notesList:List<Data> = listOf()

    inner class NoteCardViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemTitle: TextView = view.findViewById(R.id.item_title)
        val itemDetails: TextView = view.findViewById(R.id.item_details)
        val itemDate: TextView = view.findViewById(R.id.item_date)
        val itemTime: TextView = view.findViewById(R.id.item_time)
        val chipGroup:ChipGroup = view.findViewById(R.id.chip_group)
        val labelTag: TextView = view.findViewById(R.id.label_tag)
        val constraintLayout:ConstraintLayout = view.findViewById(R.id.constraint_layout_in_note_card_view)

        init {
            view.setOnLongClickListener {
                if (notesList[position] is Notes)
                    itemListener?.onLongClick(notesList[position] as Notes)
                return@setOnLongClickListener true
            }
            view.setOnClickListener {
                if (notesList[position] is Notes)
                    itemListener?.onClick(notesList[position] as Notes)
            }
        }
    }

    inner class TitleCardViewHolder(view: View):RecyclerView.ViewHolder(view){
        val titleTextView:TextView = view.findViewById(R.id.titleTextViewInNoteTitleView)
    }

    fun changeData(newList: List<Data>){
        notesList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ONE)
            NoteCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_card_view,parent,false))
        else
            TitleCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_title_card_view,parent,false))
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val data: Data = notesList[position]){
            is Notes ->{
                holder as NoteCardViewHolder
                if (data.noteTitle.isNotEmpty()) {
                    holder.itemTitle.visibility = View.VISIBLE
                    holder.itemTitle.text = data.noteTitle
                }
                else
                    holder.itemTitle.visibility = View.GONE
                if (data.noteDetails.isNotEmpty()) {
                    holder.itemDetails.visibility = View.VISIBLE
                    holder.itemDetails.text = data.noteDetails
                }
                else
                    holder.itemDetails.visibility = View.GONE
                holder.itemDate.text = data.dateCreated
                holder.itemTime.text = data.timeCreated
                val label: Set<String> = data.getLabels()
                if (label.isNotEmpty()) {
                    holder.labelTag.visibility = View.VISIBLE
                    holder.chipGroup.visibility = View.VISIBLE
                    holder.chipGroup.removeAllViews()
                    for (i in label)
                        holder.chipGroup.addChip(holder.itemView.context,i)
                }
                else{
                    holder.labelTag.visibility = View.GONE
                    holder.chipGroup.visibility = View.GONE
                    if (data.noteDetails == "")
                        holder.constraintLayout.setPadding(0,15,0,15)
                }}
            is Title ->{
                holder as TitleCardViewHolder
                holder.titleTextView.text = data.title
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (notesList[position] is Notes) VIEW_TYPE_ONE else VIEW_TYPE_TWO
    }

    private fun ChipGroup.addChip(context: Context, label:String){
        Chip(context).apply {
            text = label
            addView(this)
        }
    }

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }

}