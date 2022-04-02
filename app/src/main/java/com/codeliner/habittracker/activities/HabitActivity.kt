package com.codeliner.habittracker.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.ActivityHabitBinding
import com.codeliner.habittracker.db.HabitTaskAdapter
import com.codeliner.habittracker.db.MainViewModel
import com.codeliner.habittracker.dialogs.EditTaskDialog
import com.codeliner.habittracker.entities.HabitNameItem
import com.codeliner.habittracker.entities.HabitTaskItem
import com.codeliner.habittracker.entities.LibraryItem
import com.codeliner.habittracker.utils.ShareHelper

class HabitActivity : AppCompatActivity(), HabitTaskAdapter.Listener {
    private lateinit var bindingHAC: ActivityHabitBinding
    private var habitNameItemHAC: HabitNameItem? = null
    //32 создали переменную, где будем все хранить
    private lateinit var saveItem: MenuItem
    private var edItem: EditText? = null
    private var adapterHAc: HabitTaskAdapter? = null
    private lateinit var textWatcher: TextWatcher //43 делаем глобальной - будем использовать в нескольких функциях

    //30 так как активити, то сразу берем viewModels
    private val mainViewModelHAC: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory((applicationContext as MainApp).database)
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            bindingHAC = ActivityHabitBinding.inflate(layoutInflater)
            setContentView(bindingHAC.root)
            initHAC()
            initRcView()
            habitTaskObserver()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)    //00 добавляем стрелку назад
            setTitle(habitNameItemHAC?.name!!)
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.task_menu, menu)
        saveItem = menu?.findItem(R.id.save_task)!! //32 когда создастся и запустится наше меню, то мы найдем наш элемент и запишем сюда эту кнопку
        saveItem.isVisible = false //32 и теперь эту кнопку изначально нужно спрятать
        val newItem = menu.findItem(R.id.new_task)!! //32 нашли кнопку new task  и добавляем к ней слушатель
        edItem = newItem.actionView.findViewById(R.id.edNewTask_edAcLay) as EditText //35 при нажатии на кнопку Сохранить можем брать текст
        newItem.setOnActionExpandListener(expandActionView())
        textWatcher = textWatcher() //43 инициализировали слушатель изменений, нужно еще подключить к editText в ExpandActionView (при открытии строки добавления задачи)
        return true
    }

    private fun textWatcher(): TextWatcher { //43 функция, которая будет следить за изменениями текста
        return object : TextWatcher { //43 специальный интерфейс, который следит за изменениями в нашем editText
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { //43 ф-ция запускается при написании любой буквы, как только начинаем писать
                Log.d("@@@", "On Text Changed: $p0")
                mainViewModelHAC.getAllLibraryItems("%$p0%") //45 %% - поиск по буквам внутри слова
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }
    } //43 инициализируем в onCreateOptionsMenu

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //35 жмем на кнопку
        when (item.itemId) {
            R.id.save_task -> {
                addNewTask(edItem?.text.toString())
            }
            R.id.delete_habit -> { //41 удалить привычку с задачами
                mainViewModelHAC.deleteHabit(habitNameItemHAC?.id!!, true)
                finish() //41 закрываем наше активити
            }
            R.id.clear_tasks -> { //41 удалить список задач
                mainViewModelHAC.deleteHabit(habitNameItemHAC?.id!!, false)
            }
            R.id.share_tasks -> { //42 будем делиться нашим списком задач
                startActivity(Intent.createChooser( //42 createChooser - будем выбирать с помощью какого приложения хотим поделиться
                    ShareHelper.shareTaskList(adapterHAc?.currentList!!, habitNameItemHAC?.name!!),
                    "Share by" //42 какой текст будет
                ))
            }
        }
        if (item.itemId == android.R.id.home) finish() //00 если нажата стрелка Назад, закрываем активити
        return super.onOptionsItemSelected(item)
    }

    private fun addNewTask(name: String) { //35 нужно сохранить New Task, чтобы передать в viewModel и сохранить в БД
        if (name.isEmpty()) return //35 написано что-то в editText или нет. Если ничего, то return
        val item = HabitTaskItem( //35 если есть, то заполняем один элемент HabitTaskItem
            null,
            name,
            "", //40 передаем пустоту вместо null
            false,
            habitNameItemHAC?.id!!,
            0
        )
        edItem?.setText("") //36 после того как поместили информацию, нужно очистить поле ввода
        mainViewModelHAC.insertTask(item) //35 передаем item через mainViewModel в БД для записи
        //35 item сохранится, но чтобы увидеть в приложении,
    //35 нужно добавить RecyclerView и подключить его к Adapter и обновлять адаптер,
    // добавив observer, который будет следить за изменениями в данной таблице.
    }

    private fun habitTaskObserver() {
        mainViewModelHAC.getAllTasksFromHabit(habitNameItemHAC?.id!!).observe(this) { //36 this - наше активити
            adapterHAc?.submitList(it) //37 обновляем список, передаем адаптер
            bindingHAC.tvEmpty.visibility = if(it.isEmpty()) { //37 если список пустой
                View.VISIBLE //37 нужно показать tvEmpty (написано слово Empty)
            } else { //37 если список не пустой
                View.GONE //37 то спрятать textView
            }
        }
    } //36 нужно еще в самом адаптере заполнять элементы

    private fun libraryItemObserver() { //45 observer ждет данных из mainViewModel
        mainViewModelHAC.libraryItems.observe(this) {
            val tempTaskList = ArrayList<HabitTaskItem>() //45 создаем пустой список
            it.forEach { item -> //45 пока не переберем все library item
                val taskItem = HabitTaskItem(
                    item.id,
                    item.name,
                    "",
                    false,
                    0,
                    1 //45 разметка для подсказок (0 - стандартная разметка для ввода текста)
                )
                tempTaskList.add(taskItem)
            }
            adapterHAc?.submitList(tempTaskList)
            bindingHAC.tvEmpty.visibility = if(it.isEmpty()) { //46 если список пустой
                View.VISIBLE //46 нужно показать tvEmpty (написано слово Empty)
            } else { //46 если список не пустой
                View.GONE //46 то спрятать textView
            }
        }
    }

    private fun initRcView()= with(bindingHAC) { //36 функция для инициализации recycler view
        adapterHAc = HabitTaskAdapter(this@HabitActivity)
        rcViewAcH.layoutManager = LinearLayoutManager(this@HabitActivity)
        rcViewAcH.adapter = adapterHAc //эту fun запускаем в onCreate //36 подклюаем адаптер к recycler view
    }

    private fun expandActionView(): MenuItem.OnActionExpandListener { //32 отдельная функция, чтобы saveItem_HabAc показывался при создании нового task
        return object : MenuItem.OnActionExpandListener { //32 есть слушатель, который нужно присвоить кнопке save_task

            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                saveItem.isVisible = true //32 при создании new task нужно сделать, чтобы кнопка save task появилась
                edItem?.addTextChangedListener(textWatcher) //43 добавили слушатель изменения текста
                libraryItemObserver() //45 добавляем обсервер, когда открываем строку добавления задач
                mainViewModelHAC.getAllTasksFromHabit(habitNameItemHAC?.id!!).removeObservers(this@HabitActivity) //45 нужно отключить на время другой обсервер habitTaskObserver, чтобы ничего не ждал
                mainViewModelHAC.getAllLibraryItems("%%") //45 показать все элементы
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                saveItem.isVisible = false //32 когда new task закрывается, нужно снова прятать save
                edItem?.removeTextChangedListener(textWatcher) //43 убрали слушатель изменения текста
                invalidateOptionsMenu() //32 затем перерисовать меню, чтобы снова появилась кнопка new task
                mainViewModelHAC.libraryItems.removeObservers(this@HabitActivity) //45 убираем обсервер, когда закрываем строку добавления задач
                edItem?.setText("") //45 очищаем что есть внутри, когда закрыли строку ввода
                habitTaskObserver() //45 снова включаем основной обсервер после закрытия строки ввода задач
                return true
            }

        }
    }

    private fun initHAC() { //30 будем получать из интента HabitName, чтобы узнать какую привычку мы открыли
        habitNameItemHAC = intent.getSerializableExtra(HABIT_NAME_HAC) as HabitNameItem //30 будем передавать целый класс
    }

    companion object{
        const val HABIT_NAME_HAC = "habit_name" //30 добавляем константу, которую будем передавать
    }

    override fun onClickItem(habitTaskItem: HabitTaskItem, state: Int) {
        when(state) { //40 проверяем какое значение пришло из HabitTaskAdapter и такую команду выполняем
            HabitTaskAdapter.CHECK_BOX -> mainViewModelHAC.updateHabitTask(habitTaskItem) //40 если пришло значение чекбокс
            HabitTaskAdapter.EDIT -> editTask(habitTaskItem) //40 нужно вызывать диалог
            HabitTaskAdapter.EDIT_LIBRARY_ITEM -> editLibraryItem(habitTaskItem)
            HabitTaskAdapter.ADD -> addNewTask(habitTaskItem.name)
            HabitTaskAdapter.DELETE_LIBRARY_ITEM -> {
                mainViewModelHAC.deleteLibraryItem(habitTaskItem.id!!) //46 удаление подсказки по идентификатору
                //46 почему-то не обновляется library
                mainViewModelHAC.getAllLibraryItems("%${edItem?.text.toString()}%") //46 чтобы обновился список подсказок после удаления
            }
        } //46 константы создаем в адаптере

    }

    private fun editTask(item: HabitTaskItem) {
        EditTaskDialog.showDialog(this, item, object : EditTaskDialog.Listener {
            override fun onClick(item: HabitTaskItem) {
                mainViewModelHAC.updateHabitTask(item)
            }
        })
    }

    private fun editLibraryItem(item: HabitTaskItem) { //46 функция вызывает диалог и туда передаем HabitTaskItem
        EditTaskDialog.showDialog(this, item, object : EditTaskDialog.Listener {
            override fun onClick(item: HabitTaskItem) {
                mainViewModelHAC.updateLibraryItem(LibraryItem(item.id, item.name))
                //46 почему-то не обновляется library
                mainViewModelHAC.getAllLibraryItems("%${edItem?.text.toString()}%") //46 чтобы обновился список подсказок после изменения
            }
        })
    }

    private fun saveItemCount() { //48 считает количество выполненных задач
        var checkedItemCounter = 0 //48 чтобы посчитать сколько элементов у нас отмечено
        adapterHAc?.currentList?.forEach {
            if(it.itemChecked) checkedItemCounter++ //48 если отмечено, увеличиваем счетчик на 1
        }

        val tempTaskItem = habitNameItemHAC?.copy(
            //220316 заменить itemCounter на значение days per week
            allItemCounter = adapterHAc?.itemCount!!,  //48 сколько всего задач в привычке
            //220316 нужно заменить на сколько раз выполнено в неделю. пока 0 или 1
            checkedItemsCounter = checkedItemCounter
        )
        mainViewModelHAC.updateHabitName(tempTaskItem!!)
    }

    override fun onBackPressed() { //48 как только рещили выйти из задач, нужно отследить нажатие кнопки назад
        saveItemCount()
        super.onBackPressed()
    }
}