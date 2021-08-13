package com.example.mynotez.data.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson


class SetOfLongTypeConverter {

    @TypeConverter
    fun listToJson(value: MutableSet<Long>?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toMutableSet()
}
