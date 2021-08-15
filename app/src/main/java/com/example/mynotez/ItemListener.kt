package com.example.mynotez

import com.example.mynotez.data.Notes

interface ItemListener {

    fun onClick(note: Notes)

    fun onLongClick(note:Notes)
}