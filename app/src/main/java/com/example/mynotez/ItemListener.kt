package com.example.mynotez

interface ItemListener {

    fun onClick(position: Int)

    fun onLongClick(position: Int)
}