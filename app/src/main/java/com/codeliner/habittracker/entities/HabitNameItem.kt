package com.codeliner.habittracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "habits_list_names")
data class  HabitNameItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo (name = "id")
    val id: Int?,
    @ColumnInfo (name = "habitChecked")
    var habitChecked: Boolean = false,
    @ColumnInfo (name = "name")
    val name: String,
    @ColumnInfo (name = "time")
    val time: String,
    @ColumnInfo (name = "allItemCounter")
    val allItemCounter: Int,
    @ColumnInfo (name = "planDaysPerWeek")
    val planDaysPerWeek: String,
    @ColumnInfo (name = "checkedItemsCounter")
    val checkedItemsCounter: Int,
    @ColumnInfo (name = "checkedHabitCounter")
    val checkedHabitCounter: Int,
    @ColumnInfo (name = "checkedHabitDay")
    var checkedHabitDay: Int,
    @ColumnInfo (name = "itemsIds")
    val itemsIds: String,

    ): Serializable
