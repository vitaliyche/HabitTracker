package com.codeliner.habittracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codeliner.habittracker.activities.HabitActivity
import com.codeliner.habittracker.activities.MainApp
import com.codeliner.habittracker.databinding.FragmentHabitNamesBinding
import com.codeliner.habittracker.db.HabitAdapter
import com.codeliner.habittracker.db.MainViewModel
import com.codeliner.habittracker.dialogs.DeleteDialog
import com.codeliner.habittracker.dialogs.NewHabitDialog
import com.codeliner.habittracker.entities.HabitNameItem
import com.codeliner.habittracker.utils.TimeManager

//24 копируем класс из NoteFragment
class HabitNamesFragment : BaseFragment(), HabitAdapter.Listener {
    private lateinit var binding: FragmentHabitNamesBinding
    //27 подготавливаем переменную, чтобы инициализировать адаптер
    private lateinit var adapter: HabitAdapter

    //в mainViewModel теперь есть allNotes, insertNote и т.д. из ViewModel
    private val mainViewModel: MainViewModel by activityViewModels {
        //context превращаем в класс MainApp (инициализирующий приложение), в нем есть уже база данных
        MainViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database)
    }

    //24 будем запускать диалог, когда нажали на кнопку New
    //24 можем не прикреплять слушатель ко всему фрагменту, а добавить в функции
    override fun onClickNew() {
        NewHabitDialog.showDialog(activity as AppCompatActivity, object : NewHabitDialog.Listener {
            //24 имплементируем функцию onClick - возвращает имя, которое вписал пользователь
            override fun onClick(name: String) {
                //25 когда нажали на кнопку, прежде чем сохранить HabitName класс,
                // его нужно заполнить как в HabitsListItem
                val habitName = HabitNameItem(
                    null,
                    name,
                    TimeManager.getCurrentTime(),
                    //сколько задач добавлено уже в привычку. так как только создали, то 0
                0,
                    //сколько задач уже выполнено
                0,
                    ""
                )
                //делаем insert
                mainViewModel.insertHabit(habitName)
                //25 теперь как все запускаем, нажимаем сохранить и все сохраняется в БД
                //25 еще нужно, чтобы мы могли их видеть в фрагменте
                //25 через observer, который будет следить за изменениями в БД и считывать через MainViewModel
            }
            //29 при создании новой привычки, передаем пустоту
        }, "")
        //25 для записи в БД нужно записать insert функцию в Dao
    }

    //можем прослушивать и обновлять адаптер
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    //создание view, для управления заметками
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // инициализируем FragmentNoteBinding
        binding = FragmentHabitNamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    //функция запускается, когда все view созданы,
    //после чего можно инициализировать recyclerview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        //инициализация observer
        observer()
    }

    //инициализация recyclerView и адаптера
    //binding, чтобы напрямую использовать идентификатор
    private fun initRcView() = with(binding) {
        //27 this не можем передать, потому что binding и фрагмент,
        //27 поэтому передаем activity, потому что во фрагменте есть активити
        rcView.layoutManager = LinearLayoutManager(activity)
        //27 инициализируем адаптер
        adapter = HabitAdapter(this@HabitNamesFragment)
        //27 адаптер нужно подключить к нашему recycler view
        rcView.adapter = adapter
    }

    //25 функция запускается каждый раз, когда есть изменения в таблице для названий Привычек
    private fun observer() {
        mainViewModel.allHabits.observe(viewLifecycleOwner) {
            //25 здесь нужно обновлять adapter, который будет привязан к recycler view данного фрагмента
            //25 и здесь будет появляться новый элемент, редактироваться или удаляться, если удаляем
            //27 it - новый список, который пришел
            adapter.submitList(it)
            binding.tvEmptyHabits.visibility = if(it.isEmpty()) { //37 если список пустой
                View.VISIBLE //37 нужно показать tvEmptyHabits (написано слово Empty)
            } else { //37 если список не пустой
                View.GONE //37 то спрятать textView
            }
        }
        //25 нужно добавлять recycler view, adapter
    //25 и разметку для отдельного элемента, с помощью которого будем заполнять этот список
        //25 в разметке - название, время, прогресс бар (сколько задач выполнено и сколько осталось)
        //25 и счетчик - сколько задач выполнено и сколько задач в списке
        //25 когда все задачи выполнены, прогрес бар становится зеленым
        //25 тогда сможем смотреть как работает список
    }

    //чтобы была только одна инстанция фрагмента, если пытаемся запустить несколько раз
    companion object {
        @JvmStatic
        fun newInstance() = HabitNamesFragment()
    }

    override fun deleteItem(id: Int) { //28 имплементируем deleteItem и onClickItem
        DeleteDialog.showDialog(context as AppCompatActivity, object : DeleteDialog.Listener { //28 context может быть null, поэтому укажем его как AppCompatActivity
            override fun onClick() { //28 имплементируем onClick
                mainViewModel.deleteHabit(id, true)
            } //28 нажали на delete в нашем элементе из списка, запускается диалог,
        }) //28 который спрашивает хотим ли мы на самом деле удалить
    } //28 если жмем на кнопку Да, то запускается onClick и запускается удаление элемента

    override fun editItem(habitNameItem: HabitNameItem) { //29 делаем по аналогии с onClickNew
        NewHabitDialog.showDialog(activity as AppCompatActivity, object : NewHabitDialog.Listener {
            override fun onClick(name: String) { //24 имплементируем функцию onClick - возвращает имя, которое вписал пользователь
                mainViewModel.updateHabitName(habitNameItem.copy(name = name)) //перезаписываем название, если пользователь изменил его и нажал кнопку Обновить
            }
        }, habitNameItem.name) //29 когда обновляем, передаем название, которое было
    }

    override fun onClickItem(habitNameItem: HabitNameItem) { //30 при нажатии на весь элемент, должно открыться активити
        val i = Intent(activity, HabitActivity::class.java).apply {
            putExtra(HabitActivity.HABIT_NAME_HAC, habitNameItem)
        }
        startActivity(i)
    }
}