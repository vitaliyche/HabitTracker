package com.codeliner.habittracker.db

import androidx.lifecycle.*
import com.codeliner.habittracker.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.*

class MainViewModel(dataBase: MainDataBase): ViewModel() {

    val dao = dataBase.getDao()
    val currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) //+1 для теста
    val libraryItems = MutableLiveData<List<LibraryItem>>()
    val taskItems = MutableLiveData<List<HabitTaskItem>>()
    val allNotes: LiveData<List<NoteItem>> = dao.getAllNotes().asLiveData() //когда NoteItem будет меняться, LiveData будет автоматически обновляться //можем прослушивать есть ли изменения в заметках и обновлять адаптер
    val habitItems = getHabitsItemsFlow().asLiveData()
    val allHabits: LiveData<List<HabitNameItem>> = dao.getAllHabits().asLiveData()


    // Habit Item Model

    data class HabitItemModel(
        val id: Int,
        val checkId: Int?,
        val name: String,
        val checksCount: Int,
        val isChecked: Boolean,
        val lastWeekCheckCount: Int,
        val targetWeekCheckCount: Int
    )

    private fun getHabitsItemsFlow(): Flow<List<HabitItemModel>> {

        val habitNameItemsFlow = dao.getHabitNameItemsFlow()
        val habitCheckedItemsFlow = dao.getHabitCheckedItemsFlow()

        return habitNameItemsFlow.combine(habitCheckedItemsFlow) { habitNameItems, habitCheckedItems ->
            habitNameItems.mapNotNull { habitNameItem ->

                val filteredHabitCheckedItems = habitCheckedItems.filter { it.habitId == habitNameItem.id }
                val checkedPerWeek = filteredHabitCheckedItems.filter { it.dayOfYear > currentDayOfYear-7} //отфильтровал за неделю

                habitNameItem.id?.let {
                    HabitItemModel (
                        id = it,
                        checkId = filteredHabitCheckedItems.firstOrNull { it.dayOfYear == currentDayOfYear }?.id,
                        name = habitNameItem.name,
                        checksCount = filteredHabitCheckedItems.count(),
                        isChecked = currentDayOfYear in filteredHabitCheckedItems.map { it.dayOfYear },
                        lastWeekCheckCount = checkedPerWeek.count(), // посчитал выполненную привычку за неделю
                        targetWeekCheckCount = habitNameItem.planDaysPerWeek.toInt()
                    )
                }
            }
        }
    }

    fun insertChecked (HabitItemModel: HabitItemModel) {
        viewModelScope.launch {
            val habitId = HabitItemModel.id
            val checkedItem = HabitCheckedItem(
                habitId = habitId,
                time = Date().time,
                dayOfYear = currentDayOfYear
            )
            dao.insertCheckedItem(checkedItem)
        }
    }

    fun updateHabitName(habitNameItem: HabitItemModel) = viewModelScope.launch {
        dao.updateHabit(
            id = habitNameItem.id,
            name = habitNameItem.name,
            planDaysPerWeek = habitNameItem.targetWeekCheckCount
        )
    }


    //HabitCheckedItem

    fun getAllCheckedFromHabit(habitId: Int) : LiveData<List<HabitCheckedItem>> {
        return dao.getAllHabitChecked(habitId).asLiveData()
    }

    fun updateHabitChecked(item: HabitCheckedItem) = viewModelScope.launch {
        dao.updateCheckedItem(item)
    }

    fun deleteCheckedItem(id: Int) = viewModelScope.launch {
        dao.deleteCheckedItem(id)
    }

    fun onItemCheckChanged(habitCheckedItem: HabitCheckedItem) = viewModelScope.launch {
        // dao.onItemCheckChanged(item)
    }


    //HabitNameItem

    fun insertHabit(habitNameItem: HabitNameItem) = viewModelScope.launch { //25 функцию insert можем использовать через HabitNamesFragment
        dao.insertHabit(habitNameItem)
    }

    fun deleteHabit(id: Int, deleteHabit: Boolean) = viewModelScope.launch { //эту функцию уже можем запускать из нашего фрагмента
        if(deleteHabit) dao.deleteHabitName(id) //удаляем привычку
        dao.deleteTasksByListId(id) //40 удаляем задачи для привычки
    }


    //HabitTaskItem

    fun getAllTasksFromHabit(listId: Int) : LiveData<List<HabitTaskItem>> {
        return dao.getAllHabitTasks(listId).asLiveData()
    }

    fun getAllTaskItems(name: String) = viewModelScope.launch {
        taskItems.postValue(dao.getAllTasks(name))
    }

    fun insertTask (habitTaskItem: HabitTaskItem) = viewModelScope.launch {
        dao.insertItem(habitTaskItem) //TODO: поменять на dao.insertTaskItem
        if(!isLibraryItemExists(habitTaskItem.name)) dao.insertLibraryItem(LibraryItem(null, habitTaskItem.name)) //44 если нет элементов, только в том случае записываем
    }

    fun updateHabitTask(item: HabitTaskItem) = viewModelScope.launch {
        dao.updateHabitTask(item)
    } //39 используем функцию в Activity в onClickItem

    fun deleteTask(id: Int) = viewModelScope.launch {
        dao.deleteTask(id)
    }


    //LibraryItem

    fun getAllLibraryItems(name: String) = viewModelScope.launch { //45 запрашиваем данные из БД
        libraryItems.postValue(dao.getAllLibraryTasks(name))  //45 получаем из БД все элементы по названию и передаем в observer
    }

    fun updateLibraryItem(item: LibraryItem) = viewModelScope.launch { //46 этой функцией будем пользоваться из нашего Активити
        dao.updateLibraryItem(item)
    } //46 мы все это будем делать через адаптер (нажатие на кнопку редактирования - в адаптере срабатывает код)

    fun deleteLibraryItem(id: Int) = viewModelScope.launch {
        dao.deleteLibraryItem(id)
    } //46 можем пользоваться функцией из HabitActivity

    private suspend fun isLibraryItemExists(name: String): Boolean { //44 функция, которая по имени, которое хотим записать, будет проверять, есть ли в библиотеке такой элемент. если есть, выдаст список
        return dao.getAllLibraryTasks(name).isNotEmpty() //44 если в списке что-то есть, выдает true
    }


    //NoteItem

    fun insertNote(note: NoteItem) = viewModelScope.launch {
        dao.insertNote(note)
    } //использую корутины, чтобы записать данные на второстепенном потоке //не будет захламляться основной поток, в котором рисуется пользовательский интерфейс

    fun updateNote(note: NoteItem) = viewModelScope.launch {
        dao.updateNote(note)
    }

    fun deleteNote(id: Int) = viewModelScope.launch {
        dao.deleteNote(id)
    }


    class MainViewModelFactory(val dataBase: MainDataBase): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(dataBase) as T //инициализируем MainViewModel
            }
            throw IllegalArgumentException("Unknown ViewModelClass")
        }
    } //каждый раз, когда создаем ViewModel, создаем ViewModelFactory, всегда один и тот же


}

