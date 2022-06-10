package com.codeliner.habittracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "habit_checked_counter")
data class  HabitCheckedCounter(
    @PrimaryKey (autoGenerate = true)
    val id: Int?,
    @ColumnInfo (name = "name")
    val name: String,
    @ColumnInfo (name = "time")
    val time: String,
    @ColumnInfo (name = "checkedHabitCounter")
    val checkedHabitCounter: Int,
    @ColumnInfo (name = "checkedHabitDay")
    var checkedHabitDay: Int
    ): Serializable
