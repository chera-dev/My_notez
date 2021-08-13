package com.example.mynotez.data.repository

import androidx.lifecycle.LiveData
import com.example.mynotez.data.Label
import com.example.mynotez.data.dao.LabelDao

class LabelRepository(private val labelDao: LabelDao) {

    val readAllLabel: LiveData<List<Label>> = labelDao.readAllLabel()

    suspend fun addLabel(label: Label){
        labelDao.addLabel(label)
    }

    suspend fun updateLabel(label: Label){
        labelDao.updateLabel(label)
    }

    suspend fun deleteLabel(label: Label){
        labelDao.deleteLabel(label)
    }

    //not
    suspend fun getLabelByName(labelName:String): Label?{
        return labelDao.getLabelByName(labelName)
    }


}