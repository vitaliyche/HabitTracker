package com.codeliner.habittracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codeliner.habittracker.entities.HabitNameItem
import com.codeliner.habittracker.entities.HabitTaskItem
import com.codeliner.habittracker.entities.LibraryItem
import com.codeliner.habittracker.entities.NoteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Query ("SELECT * FROM note_list")
    fun getAllNotes(): Flow<List<NoteItem>> //Flow - не нужно указывать suspend
    @Query ("SELECT * FROM habits_list_names") //25 выбрать все привычки из таблицы habits_list_names
    fun getAllHabits(): Flow<List<HabitNameItem>> //25 и выдать их, после чего описываем функцию в MainViewModel

    @Query ("SELECT * FROM habits_list_item WHERE listId LIKE :listId")
    fun getAllHabitTasks(listId: Int): Flow<List<HabitTaskItem>> //36 этой функцией нужно пользоваться естественно из viewModel класса

    @Query ("SELECT * FROM library WHERE name LIKE :name")
    suspend fun getAllLibraryTasks(name: String): List<LibraryItem> //44 получить  из библиотеки все элементы

    @Query ("SELECT * FROM habits_list_item WHERE name LIKE :name")
    suspend fun getAllTasks(name: String): List<HabitTaskItem>


    @Query ("DELETE FROM note_list WHERE id IS :id") //удаляем из списка note_list запись, где id равен переданному id из функции
    suspend fun deleteNote(id: Int) //suspend - если хотим запустить в корутине, а не на второстепенном потоке (где выдаст ошибку)
    @Query ("DELETE FROM habits_list_names WHERE id IS :id")
    suspend fun deleteHabitName(id: Int)
    @Query ("DELETE FROM habits_list_item WHERE listId LIKE :listId")
    suspend fun deleteTasksByListId(listId: Int)
    @Query ("DELETE FROM library WHERE id IS :id")
    suspend fun deleteLibraryItem(id: Int)
    @Query ("DELETE FROM habits_list_item WHERE id IS :id")
    suspend fun deleteTask(id: Int)

    @Insert
    suspend fun insertNote(note: NoteItem)
    @Insert
    suspend fun insertHabit(nameItem: HabitNameItem) //25 функцию запускаем,естественно, не напрямую, а через ViewModel в MainViewModel
    @Insert
    suspend fun insertItem(habitTaskItem: HabitTaskItem)
    @Insert
    suspend fun insertLibraryItem(libraryItem: LibraryItem)


    @Update
    suspend fun updateNote(note: NoteItem)
    @Update
    suspend fun updateHabitTask(item: HabitTaskItem) //39 функцию использовать через класс MainViewModel
    @Update
    suspend fun updateHabitName(habitNameItem: HabitNameItem)
    @Update
    suspend fun updateLibraryItem(item: LibraryItem) //46 пользуемся через mainViewmodel
} //dao пользуемся через ViewModel, поэтому там тоже создаем
