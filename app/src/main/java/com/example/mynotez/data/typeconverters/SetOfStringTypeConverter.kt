package com.example.mynotez.data.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson


class SetOfStringTypeConverter {

    @TypeConverter
    fun listToJson(value: MutableSet<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<String>::class.java).toMutableSet()
}