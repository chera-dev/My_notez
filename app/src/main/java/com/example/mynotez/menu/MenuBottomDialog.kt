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
        val item = BottomMenuTextViewItemBinding.inflate(layoutInflater)
        item.imageViewInBottomSheet.setImageResource(operation.imageResource)
        item.textViewInBottomSheet.text = operation.titleText
        item.textViewInBottomSheet.setOnClickListener {
            dismiss()
            operation.operation.invoke()
        }
        item.imageViewInBottomSheet.setOnClickListener {
            dismiss()
            operation.operation.invoke()
        }
        linearLayout.addView(item.root)
    }

    data class Operation(val titleText: String, val imageResource:Int, val operation: () -> Unit)
}