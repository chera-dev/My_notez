package com.example.mynotez.fragment.note

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.*
import com.example.mynotez.data.entities.Notes
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class NotesAdapter ( private val itemListener: ItemListener?)
    : RecyclerView.Adapter<NotesAdapter.NoteCardViewHolder>() {

    var notesList:List<Notes> = listOf()

    inner class NoteCardViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemTitle: TextView = view.findViewById(R.id.item_title)
        val itemDetails: TextView = view.findViewById(R.id.item_details)
        val itemDate: TextView = view.findViewById(R.id.item_date)
        val itemTime: TextView = view.findViewById(R.id.item_time)
        val chipGroup:ChipGroup = view.findViewById(R.id.chip_group)
        val labelTag: TextView = view.findViewById(R.id.label_tag)
        val itemPinned: TextView = view.findViewById(R.id.item_pinned)

        init {
            view.setOnLongClickListener {
                itemListener?.onLongClick(notesList[position])
                return@setOnLongClickListener true
            }
            view.setOnClickListener {
                itemListener?.onClick(notesList[position])
            }
        }
    }

    fun changeData(newList: List<Notes>){
        notesList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteCardViewHolder {
        return NoteCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_card_view,parent,false))
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    override fun onBindViewHolder(holder: NoteCardViewHolder, position: Int) {

        val data: Notes = notesList[position]
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
        if(data.isPinned){
            holder.itemPinned.visibility = View.VISIBLE
        }
        else{
            holder.itemPinned.visibility = View.GONE
        }
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
        }
    }

    private fun ChipGroup.addChip(context: Context, label:String){
        Chip(context).apply {
            text = label
            addView(this)
        }
    }
}