package com.codeliner.habittracker.db

import androidx.lifecycle.*
import com.codeliner.habittracker.entities.HabitNameItem
import com.codeliner.habittracker.entities.HabitTaskItem
import com.codeliner.habittracker.entities.LibraryItem
import com.codeliner.habittracker.entities.NoteItem
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MainViewModel(dataBase: MainDataBase): ViewModel() { //не используем напрямую бизнес-логику, через viewmodel
    val dao = dataBase.getDao()
    val libraryItems = MutableLiveData<List<LibraryItem>>()

    val allNotes: LiveData<List<NoteItem>> = dao.getAllNotes().asLiveData() //когда NoteItem будет меняться, LiveData будет автоматически обновляться //можем прослушивать есть ли изменения в заметках и обновлять адаптер
    //25 получаем все Привычки, которые есть в БД
    val allHabits: LiveData<List<HabitNameItem>> = dao.getAllHabits().asLiveData() //25 теперь можем добавить observer в live data, //25 который будет обновляться каждый раз, когда в этой таблице что-то меняется
    //идем в HabitsNameFragment

    fun getAllTasksFromHabit(listId: Int) : LiveData<List<HabitTaskItem>> {
        return dao.getAllHabitTasks(listId).asLiveData()
    }

    fun getAllLibraryItems(name: String) = viewModelScope.launch { //45 запрашиваем данные из БД
        libraryItems.postValue(dao.getAllLibraryTasks(name))  //45 получаем из БД все элементы по названию и передаем в observer
    }

    fun insertNote(note: NoteItem) = viewModelScope.launch { //используем корутины, чтобы записать данные на второстепенном потоке //не будет захламляться основной поток, в котором рисуетс пользовательский интерфейс
        dao.insertNote(note)
    }

    fun insertHabit(habitNameItem: HabitNameItem) = viewModelScope.launch { //25 функцию insert можем использовать через HabitNamesFragment
        dao.insertHabit(habitNameItem)
    }

    fun insertTask (habitTaskItem: HabitTaskItem) = viewModelScope.launch {
        dao.insertItem(habitTaskItem)
        if(!isLibraryItemExists(habitTaskItem.name)) dao.insertLibraryItem(LibraryItem(null, habitTaskItem.name)) //44 если нет элементов, только в том случае записываем
    }

    fun updateHabitTask(item: HabitTaskItem) = viewModelScope.launch {
        dao.updateHabitTask(item)
    } //39 используем функцию в Activity в onClickItem

    fun updateNote(note: NoteItem) = viewModelScope.launch { //такая же, как insert, только update
        dao.updateNote(note)
    } //чтобы ее использовать, нужно вернуться к NoteFragment и сделать проверку

    fun updateHabitName(habitNameItem: HabitNameItem) = viewModelScope.launch {
        dao.updateHabitName(habitNameItem)
    } //после view model делаем кнопку в адаптере

    fun updateLibraryItem(item: LibraryItem) = viewModelScope.launch { //46 этой функцией будем пользоваться из нашего Активити
        dao.updateLibraryItem(item)
    } //46 мы все это будем делать через адаптер (нажатие на кнопку редактирования - в адаптере срабатывает код)

    fun deleteNote(id: Int) = viewModelScope.launch { //добавляем функцию удаления по типу insert
        dao.deleteNote(id)
    } //и остается создать функцию deleteNote в dao

    fun deleteHabit(id: Int, deleteHabit: Boolean) = viewModelScope.launch { //эту функцию уже можем запускать из нашего фрагмента
        if(deleteHabit) dao.deleteHabitName(id) //удаляем привычку
        dao.deleteTasksByListId(id) //40 удаляем задачи для привычки
    }

    fun deleteLibraryItem(id: Int) = viewModelScope.launch {
        dao.deleteLibraryItem(id)
    } //46 можем пользоваться функцией из HabitActivity

    private suspend fun isLibraryItemExists(name: String): Boolean { //44 функция, которая по имени, которое хотим записать, будет проверять, есть ли в библиотеке такой элемент. если есть, выдаст список
        return dao.getAllLibraryTasks(name).isNotEmpty() //44 если в списке что-то есть, выдает true
    }
    class MainViewModelFactory(val dataBase: MainDataBase): ViewModelProvider.Factory{ //каждый раз, когда создаем ViewModel, создаем ViewModelFactory, всегда один и тот же //каждый раз, когда создаем ViewModel, создаем ViewModelFactory, всегда один и тот же
        override fun <T : ViewModel> create(modelClass: Class<T>): T { //имплементируем функцию по умолчанию
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(dataBase) as T //инициализируем MainViewModel
            }
            throw IllegalArgumentException("Unknown ViewModelClass")
        }
    }
} //дальше инициализация в NoteFragment

