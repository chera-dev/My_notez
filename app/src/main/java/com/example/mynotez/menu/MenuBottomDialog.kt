package com.example.mynotez.menu

import android.content.Context
import android.widget.LinearLayout
import com.example.mynotez.databinding.BottomMenuTextViewItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MenuBottomDialog (context: Context): BottomSheetDialog(context) {
    private val linearLayout = LinearLayout(context)
    init {
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        setContentView(linearLayout)
    }
    fun addTextViewItem(operation: Operation) = apply {
        val item = BottomMenuTextViewItemBinding.inflate(layoutInflater).root
        item.text = operation.textId
        item.setOnClickListener {
            dismiss()
            operation.operation.invoke()
        }
        linearLayout.addView(item)
    }

    data class Operation(val textId: String, val operation: () -> Unit)
}