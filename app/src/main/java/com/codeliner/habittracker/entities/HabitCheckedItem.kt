package com.codeliner.habittracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "habit_checked_item")
data class  HabitCheckedItem(
    @PrimaryKey (autoGenerate = true)
    val id: Int?,
    @ColumnInfo (name = "habitId") //36 к какой привычке задачи принадлежат
    val habitId: String,
    @ColumnInfo (name = "time")
    val time: String,
//    @ColumnInfo (name = "checkedHabitCounter")
//    val checkedHabitCounter: Int,
    @ColumnInfo (name = "checkedHabitDay")
    var checkedHabitDay: Int
    ): Serializable
