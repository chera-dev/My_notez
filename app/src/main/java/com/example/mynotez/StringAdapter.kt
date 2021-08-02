package com.example.mynotez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class StringAdapter  (val list:List<Label>): RecyclerView.Adapter<StringAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemLabel: MaterialTextView = itemView.findViewById(R.id.item_label_in_note)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.label_in_note_card_view,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemLabel.text = list[position].labelName
    }

    override fun getItemCount(): Int {
        return list.size
    }
}