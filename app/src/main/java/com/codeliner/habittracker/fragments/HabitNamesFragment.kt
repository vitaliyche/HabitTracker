package com.codeliner.habittracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeliner.habittracker.activities.HabitActivity
import com.codeliner.habittracker.activities.MainApp
import com.codeliner.habittracker.databinding.FragmentHabitNamesBinding
import com.codeliner.habittracker.db.HabitAdapter
import com.codeliner.habittracker.db.MainViewModel
import com.codeliner.habittracker.dialogs.DeleteDialog
import com.codeliner.habittracker.dialogs.NewHabitDialog
import com.codeliner.habittracker.entities.HabitNameItem
import com.codeliner.habittracker.entities.LibraryItem
import com.codeliner.habittracker.utils.TimeManager
import kotlinx.android.synthetic.main.habit_name_item.*
import java.util.*
import kotlin.collections.ArrayList

class HabitNamesFragment : BaseFragment(), //24 копируем класс из NoteFragment
    HabitAdapter.Listener {
    val items = mutableListOf<HabitNameItem>()
    private lateinit var binding: FragmentHabitNamesBinding
    private lateinit var adapter: HabitAdapter //27 подготавливаем переменную, чтобы инициализировать адаптер

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database) //в mainViewModel теперь есть allNotes, insertNote и т.д. из ViewModel
    } //context превращаем в класс MainApp (инициализирующий приложение), в нем есть уже база данных

    override fun onClickNew() { //24 будем запускать диалог, когда нажали на кнопку New //24 можем не прикреплять слушатель ко всему фрагменту, а добавить в функции
        NewHabitDialog.showDialog(activity as AppCompatActivity, object : NewHabitDialog.Listener {
            override fun onClick(
                name: String,
                days: String
            ) { //24 имплементируем функцию onClick - возвращает имя, которое вписал пользователь
                val habitName = HabitNameItem( //25 когда нажали на кнопку, прежде чем сохранить HabitName класс, // его нужно заполнить как в HabitsListItem
                        null,
                        false,
                        name,
                        TimeManager.getCurrentTime(),
                        0, //сколько задач добавлено уже в привычку. так как только создали, то 0
                        days, //сколько задач уже выполнено
                        0,
                        0,
                        0, //Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                        ""
                    )
                mainViewModel.insertHabit(habitName) //делаем insert //25 теперь как все запускаем, нажимаем сохранить и все сохраняется в БД
            } //25 еще нужно, чтобы мы могли их видеть в фрагменте //25 через observer, который будет следить за изменениями в БД и считывать через MainViewModel
        }, "", "") //29 при создании новой привычки, передаем пустоту
    } //25 для записи в БД нужно записать insert функцию в Dao

    private var simplecallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START
                or ItemTouchHelper.END, 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition // start position
            val endPosition = target.adapterPosition //endPosition
            //Collections.swap(items, startPosition, endPosition) //нужно понять что поместить в items
            //adapter.submitList(items)
            adapter.notifyItemMoved(startPosition,endPosition) //notify the adapter about item moved
            return false
        } //this is for the drag and drop feature

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //swipe to delete feature
        }
    }

    override fun onCreateView( //создание view, для управления заметками
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHabitNamesBinding.inflate(inflater, container, false) // инициализируем FragmentNoteBinding
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle? //функция запускается, когда все view созданы, //после чего можно инициализировать recyclerview
    ) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        observer() //инициализация observer
        // должно типа обнулять галочки, но не обнуляет
    }

    //инициализация recyclerView и адаптера //binding, чтобы напрямую использовать идентификатор
    private fun initRcView() = with(binding) {
        rcView.layoutManager = LinearLayoutManager(activity) //27 this не можем передать, потому что binding и фрагмент, //27 поэтому передаем activity, потому что во фрагменте есть активити
        adapter = HabitAdapter(this@HabitNamesFragment) //27 инициализируем адаптер
        rcView.adapter = adapter //27 адаптер нужно подключить к нашему recycler view

        val recyclerView : RecyclerView = rcView //findViewById (R.id.rcView)
        //rcView.layoutManager = LinearLayoutManager(activity)
        val itemTouchHelper = ItemTouchHelper(simplecallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    //25 функция запускается каждый раз, когда есть изменения в таблице для названий Привычек
    private fun observer() {
        mainViewModel.allHabits.observe(viewLifecycleOwner) { habitItems ->
            if (!IS_HABITS_RESET) {
                val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                habitItems.forEach {
                    if (it.checkedHabitDay != today) {
                        it.checkedHabitDay = today //присовить сегодняшний день
                        it.habitChecked = false //сделать привычку невыполненной
                    }
                }
                IS_HABITS_RESET = true
            } //если открыли приложение

            adapter.submitList(habitItems) //25 здесь нужно обновлять adapter, который будет привязан к recycler view данного фрагмента //25 и здесь будет появляться новый элемент, редактироваться или удаляться, если удаляем //27 it - новый список, который пришел
            items.addAll(habitItems)
            binding.tvEmptyHabits.visibility = if (habitItems.isEmpty()) { //37 если список пустой
                View.VISIBLE //37 нужно показать tvEmptyHabits (написано слово Empty)
            } else { //37 если список не пустой
                View.GONE //37 то спрятать textView
            }
        } //25 нужно добавлять recycler view, adapter //25 и разметку для отдельного элемента, с помощью которого будем заполнять этот список //25 в разметке - название, время, прогресс бар (сколько задач выполнено и сколько осталось) //25 и счетчик - сколько задач выполнено и сколько задач в списке //25 когда все задачи выполнены, прогрес бар становится зеленым //25 тогда сможем смотреть как работает список
    }

    override fun deleteItem(id: Int) {
        DeleteDialog.showDialog(
            context as AppCompatActivity,
            object :
                DeleteDialog.Listener { //28 context может быть null, поэтому укажем его как AppCompatActivity
                override fun onClick() {
                    mainViewModel.deleteHabit(id, true)
                } //28 нажали на delete в нашем элементе из списка, запускается диалог,
            }) //28 который спрашивает хотим ли мы на самом деле удалить
    } //28 если жмем на кнопку Да, то запускается onClick и запускается удаление элемента

    override fun editItem(habitNameItem: HabitNameItem) { //29 делаем по аналогии с onClickNew
        NewHabitDialog.showDialog(
            activity as AppCompatActivity,
            object : NewHabitDialog.Listener {
                override fun onClick(
                    name: String,
                    days: String
                ) { //24 имплементируем функцию onClick - возвращает имя, которое вписал пользователь
                    mainViewModel.updateHabitName(
                        habitNameItem.copy(
                            name = name,
                            planDaysPerWeek = days
                        )
                    ) //перезаписываем название, если пользователь изменил его и нажал кнопку Обновить
                }
            },
            habitNameItem.name,
            habitNameItem.planDaysPerWeek
        ) //29 когда обновляем, передаем название, которое было
    }

    override fun onClickItem(habitNameItem: HabitNameItem, state: Int) {
        when (state) { //220315 проверить на какой элемент строки нажали
            HabitAdapter.CHECK_BOX -> mainViewModel.updateHabitName(habitNameItem) //220315 записать значение в БД
            HabitAdapter.NAME -> { //220315 если нажали на название Привычки
                val i = Intent(activity, HabitActivity::class.java).apply {
                    putExtra(HabitActivity.HABIT_NAME_HAC, habitNameItem)
                }
                startActivity(i)
            }
        }
    }

    companion object {
        private var IS_HABITS_RESET = false //если галочки сброшены

        @JvmStatic //чтобы была только одна инстанция фрагмента, если пытаемся запустить несколько раз
        fun newInstance() = HabitNamesFragment()
    }
}