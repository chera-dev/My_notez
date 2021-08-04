package com.example.mynotez.fragment.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotez.*
import com.example.mynotez.Note.Companion.PINNED
import com.example.mynotez.viewmodel.SharedViewModel

class NotesAdapter (var notesList:List<Note>, private val itemListener: ItemListener?, private val sharedViewModel: SharedViewModel?)
    : RecyclerView.Adapter<NotesAdapter.NoteCardViewHolder>() {

    inner class NoteCardViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemTitle: TextView = view.findViewById(R.id.item_title)
        val itemDetails: TextView = view.findViewById(R.id.item_details)
        val itemDate: TextView = view.findViewById(R.id.item_date)
        val itemTime: TextView = view.findViewById(R.id.item_time)
        val recyclerView: RecyclerView = view.findViewById(R.id.label_recycler_view)
        val labelTag: TextView = view.findViewById(R.id.label_tag)
        val itemPinned: TextView = view.findViewById(R.id.item_pinned)

        init {
            view.setOnLongClickListener {
                itemListener?.onLongClick(position)
                return@setOnLongClickListener true
            }
            view.setOnClickListener {
                itemListener?.onClick(position)
            }
        }
    }

    fun changeData(newList: List<Note>){
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

        val data: Note = notesList[position]
        if (data.noteTitle.isNotEmpty()) {
            holder.itemTitle.visibility = View.VISIBLE
            holder.itemTitle.text = data.noteTitle
        }
        if (data.noteDetails.isNotEmpty()) {
            holder.itemDetails.visibility = View.VISIBLE
            holder.itemDetails.text = data.noteDetails
        }
        holder.itemDate.text = data.dateCreated
        holder.itemTime.text = data.timeCreated
        if(data.pinned == PINNED){
            holder.itemPinned.visibility = View.VISIBLE
        }
        else{
            holder.itemPinned.visibility = View.GONE
        }
        if (sharedViewModel!=null){
            val label: MutableSet<Label> = sharedViewModel.getSetOfLabelsOfThisNote(data.noteId)
            if (label.isNotEmpty()) {
                holder.labelTag.visibility = View.VISIBLE
                holder.recyclerView.visibility = View.VISIBLE
                val adapter = LabelAdapter(label)
                holder.recyclerView.adapter = adapter
                holder.recyclerView.layoutManager = LinearLayoutManager(
                    holder.itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
            else{
                holder.labelTag.visibility = View.GONE
                holder.recyclerView.visibility = View.GONE
            }
        }
    }

}