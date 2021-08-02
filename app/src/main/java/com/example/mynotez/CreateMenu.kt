package com.example.mynotez

import android.view.Menu
import android.view.MenuItem

class CreateMenu (val menu: Menu) {
    fun addMenuItem(groupId:Int, itemId:Int, order:Int, title:String, iconId:Int,
                    actionFlag:Int,onclick:(String) -> Unit= { itemTitle:String -> }): MenuItem {
        return menu.add(groupId, itemId, order, title).setIcon(iconId)
            .setShowAsActionFlags(actionFlag).setOnMenuItemClickListener {
                onclick(title)
                true
            }
    }
    fun changeIcon(itemId: Int,iconId: Int){
        menu.getItem(itemId).setIcon(iconId)
    }
}