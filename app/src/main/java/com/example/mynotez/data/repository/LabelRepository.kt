package com.example.mynotez.data.repository

import androidx.lifecycle.LiveData
import com.example.mynotez.data.entities.Label
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

    suspend fun renameLabel(oldLabelName:String ,newLabelName:String){
        labelDao.renameLabel(oldLabelName,newLabelName)
    }

}