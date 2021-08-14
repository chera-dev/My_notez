package com.example.mynotez.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mynotez.data.Label
import com.example.mynotez.data.Notes

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
    fun getLabelByName(labelName:String): Label?

    //#
    @Query("SELECT * FROM label_table WHERE label_name IN (:labelNames)")
    fun getLabelByNames(labelNames: Set<String>):LiveData<List<Label>>

    @Query("UPDATE label_table SET label_name =:newLabelName WHERE label_name=:oldLabelName")
    suspend fun renameLabel(oldLabelName:String ,newLabelName:String)

}