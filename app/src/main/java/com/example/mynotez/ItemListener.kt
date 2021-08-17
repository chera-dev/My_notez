package com.example.mynotez

import com.example.mynotez.data.entities.Notes

interface ItemListener {

    fun onClick(note: Notes)

    fun onLongClick(note: Notes)
}