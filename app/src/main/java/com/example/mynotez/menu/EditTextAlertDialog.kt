package com.example.mynotez.menu

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.example.mynotez.R

class EditTextAlertDialog {
    fun createAlertDialog(context: Context,layoutInflater: LayoutInflater,title:String,text:String?,
                          positiveButtonName:String, onPositiveButtonClick:(String) -> Unit = {}){
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        builder.setTitle(title)
        val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
        val titleEditText = dialogLayout.findViewById<EditText>(R.id.label_title_edit_text)
        builder.setView(dialogLayout)
        builder.setPositiveButton(positiveButtonName){ _, _ ->
            onPositiveButtonClick(titleEditText.text.toString())
        }
        builder.setNegativeButton("Cancel"){ _, _ ->
        }
        val alertDialog = builder.show()
        titleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        if (text != null) {
            titleEditText.setText(text)
            titleEditText.setSelectAllOnFocus(true)
        }
        titleEditText.requestFocus()
        titleEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE ) {
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                    return true
                }
                return false
            }
        })

    }
}