package com.codeliner.habittracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "habit_checked_item")
data class  HabitCheckedItem(
    @PrimaryKey (autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo (name = "habitId") //36 к какой привычке задачи принадлежат
    val habitId: Int,
    @ColumnInfo (name = "time")
    val time: Long,
    @ColumnInfo (name = "dayOfYear")
    val dayOfYear: Int,
    //TODO: удалить, если день отметки будет сохраняться в time
//    @ColumnInfo (name = "checkedHabitDay")
//    var checkedHabitDay: Int,
    ): Serializable
