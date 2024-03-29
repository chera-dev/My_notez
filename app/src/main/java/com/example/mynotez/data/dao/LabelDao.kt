package com.example.mynotez.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotez.data.entities.Label

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLabel(label: Label)

    @Update
    suspend fun updateLabel(label: Label)

    @Delete
    suspend fun deleteLabel(label: Label)

    @Query("SELECT * FROM label_table")
    fun readAllLabel(): LiveData<List<Label>>

    @Query("UPDATE label_table SET label_name =:newLabelName WHERE label_name=:oldLabelName")
    suspend fun renameLabel(oldLabelName:String ,newLabelName:String)

}