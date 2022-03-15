package com.codeliner.habittracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "habits_list_item")
data class HabitTaskItem(
    @PrimaryKey (autoGenerate = true)
    val id: Int?,
    @ColumnInfo (name = "name")
    val name: String,
    @ColumnInfo (name = "itemInfo")
    val itemInfo: String = "", //40 изначально пусто, чтобы не прописывать null в EditTaskDialog
    @ColumnInfo (name = "itemChecked")
    val itemChecked: Boolean = false,
    @ColumnInfo (name = "listId") //36 к какой привычке задачи принадлежат
    val listId: Int,
    @ColumnInfo (name = "itemType")
    val itemType: Int = 0
)
