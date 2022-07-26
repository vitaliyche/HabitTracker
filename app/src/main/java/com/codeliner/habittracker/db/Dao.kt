package com.codeliner.habittracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codeliner.habittracker.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    //HabitCheckedItem
    @Query ("SELECT * FROM habit_checked_item WHERE habitId LIKE :habitId")
    fun getAllHabitChecked(habitId: Int): Flow<List<HabitCheckedItem>>
    //TODO: выдает ошибку - исправить
    /*@Query ("SELECT * FROM habit_checked_item WHERE habitId LIKE :habitId")
    suspend fun getSuspendAllHabitChecked(habitId: Int, date: Long): List<HabitCheckedItem>*/
    @Query ("DELETE FROM habit_checked_item WHERE habitId LIKE :habitId")
    suspend fun deleteCheckedByHabitId(habitId: Int)
    @Query ("DELETE FROM habit_checked_item WHERE id IS :id")
    suspend fun deleteCheckedItem(id: Int)
    @Insert
    suspend fun insertCheckedItem(habitCheckedItem: HabitCheckedItem)
    @Update
    suspend fun updateCheckedItem(item: HabitCheckedItem)

    @Query ("SELECT * FROM habit_checked_item")
    fun getHabitCheckedItemsFlow(): Flow<List<HabitCheckedItem>>

    @Query ("SELECT * FROM habits_list_names")
    fun getHabitNameItemsFlow(): Flow<List<HabitNameItem>>



    //HabitNameItem
    @Query ("SELECT * FROM habits_list_names") //25 выбрать все привычки из таблицы habits_list_names
    fun getAllHabits(): Flow<List<HabitNameItem>> //25 и выдать их, после чего описываем функцию в MainViewModel
    @Query ("DELETE FROM habits_list_names WHERE id IS :id")
    suspend fun deleteHabitName(id: Int)
    @Insert
    suspend fun insertHabit(nameItem: HabitNameItem) //25 функцию запускаем,естественно, не напрямую, а через ViewModel в MainViewModel
    @Update
    suspend fun updateHabitName(habitNameItem: HabitNameItem)


    //HabitTaskItem
    @Query ("SELECT * FROM habits_list_item WHERE listId LIKE :listId")
    fun getAllHabitTasks(listId: Int): Flow<List<HabitTaskItem>> //36 этой функцией нужно пользоваться естественно из viewModel класса
    @Query ("SELECT * FROM habits_list_item WHERE name LIKE :name")
    suspend fun getAllTasks(name: String): List<HabitTaskItem>
    @Query ("DELETE FROM habits_list_item WHERE listId LIKE :listId")
    suspend fun deleteTasksByListId(listId: Int)
    @Query ("DELETE FROM habits_list_item WHERE id IS :id")
    suspend fun deleteTask(id: Int)
    @Insert
    suspend fun insertItem(habitTaskItem: HabitTaskItem) //TODO: поменять на insertTaskItem
    @Update
    suspend fun updateHabitTask(item: HabitTaskItem) //39 функцию использовать через класс MainViewModel


    //LibraryItem
    @Query ("SELECT * FROM library WHERE name LIKE :name")
    suspend fun getAllLibraryTasks(name: String): List<LibraryItem> //44 получить  из библиотеки все элементы
    @Query ("DELETE FROM library WHERE id IS :id")
    suspend fun deleteLibraryItem(id: Int)
    @Insert
    suspend fun insertLibraryItem(libraryItem: LibraryItem)
    @Update
    suspend fun updateLibraryItem(item: LibraryItem) //46 пользуемся через mainViewmodel


    //NoteItem
    @Query ("SELECT * FROM note_list")
    fun getAllNotes(): Flow<List<NoteItem>> //Flow - не нужно указывать suspend
    @Query ("DELETE FROM note_list WHERE id IS :id")
    suspend fun deleteNote(id: Int) //suspend - если хотим запустить в корутине, а не на второстепенном потоке (где выдаст ошибку)
    @Insert
    suspend fun insertNote(note: NoteItem)
    @Update
    suspend fun updateNote(note: NoteItem)

} //dao пользуемся через ViewModel, поэтому там тоже создаем
