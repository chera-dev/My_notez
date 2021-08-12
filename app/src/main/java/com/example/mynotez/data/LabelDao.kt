package com.example.mynotez.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLabel(label: Label)

    @Update
    suspend fun updateLabel(label: Label)

    @Delete
    suspend fun deleteLabel(label: Label)

    @Query("SELECT * FROM label_table ORDER BY `order` DESC")
    fun readAllLabel(): LiveData<List<Label>>

    //not
    @Query("SELECT * FROM label_table WHERE label_name =:labelName LIMIT 1")
    fun getLabelByName(labelName:String):Label?
}