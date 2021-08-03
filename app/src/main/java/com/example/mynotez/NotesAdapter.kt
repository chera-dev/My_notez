package com.example.mynotez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter (var notesList:List<Note>, private val itemListener: ItemListener?, private val sharedViewModel: SharedViewModel?)
    : RecyclerView.Adapter<NotesAdapter.NoteCardViewHolder>() {

    inner class NoteCardViewHolder(view: View): RecyclerView.ViewHolder(view){
        val itemTitle: TextView
        val itemDetails: TextView
        val itemDate: TextView
        val itemTime: TextView
        val recyclerView: RecyclerView
        val labelTag: TextView

        init {
            itemTitle = view.findViewById(R.id.item_title)
            itemDetails = view.findViewById(R.id.item_details)
            itemDate = view.findViewById(R.id.item_date)
            itemTime = view.findViewById(R.id.item_time)
            recyclerView = view.findViewById(R.id.label_recycler_view)
            labelTag = view.findViewById(R.id.label_tag)

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

        val data:Note = notesList[position]
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
        if (sharedViewModel!=null){
            val label: Set<Label> = sharedViewModel.getSetOfLabelsOfThisNote(data.noteId)
            if (label.isNotEmpty()) {
                holder.labelTag.visibility = View.VISIBLE
                val adapter = LabelAdapter(label as MutableSet<Label>)
                holder.recyclerView.adapter = adapter
                holder.recyclerView.layoutManager = LinearLayoutManager(
                    holder.itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }
    }

}