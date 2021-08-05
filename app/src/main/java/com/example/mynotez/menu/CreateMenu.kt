package com.example.mynotez.menu

import android.view.Menu
import android.view.MenuItem

class CreateMenu (private val menu: Menu) {
    fun addMenuItem(groupId:Int, itemId:Int, order:Int, title:String, iconId:Int,
                    actionFlag:Int,onclick:(String) -> Unit= { }): MenuItem {
        return menu.add(groupId, itemId, order, title).setIcon(iconId)
            .setShowAsActionFlags(actionFlag).setOnMenuItemClickListener {
                onclick(title)
                true
            }
    }
    fun changeIcon(itemId: Int,iconId: Int){
        menu.getItem(itemId-1).setIcon(iconId)
    }
}