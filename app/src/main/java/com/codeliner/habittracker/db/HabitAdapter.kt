package com.codeliner.habittracker.db

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.HabitNameItemBinding
import com.codeliner.habittracker.entities.HabitCheckedItem
import com.codeliner.habittracker.entities.HabitNameItem
import com.codeliner.habittracker.utils.TimeManager
import java.util.*


class HabitAdapter(
    private val listener: Listener, // нужно передать в setData
    //TODO: передать checkedListener для сохранения данных в таблицу HabitCheckedItem
    //private val checkedlistener: CheckedListener
    ): ListAdapter<MainViewModel.HabitItemModel, HabitAdapter.ItemHolder>(ItemComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }


    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener) // listener присваивается каждому элементу
    }


    class ItemHolder(view: View): RecyclerView.ViewHolder(view) {

        private val binding = HabitNameItemBinding.bind(view)

        fun setHabitNameItemData(habitNameItem: HabitNameItem, listener: Listener) {

        }

        fun setData(habitNameItem: MainViewModel.HabitItemModel, listener: Listener) = with(binding) {

            tvHabitName.text = habitNameItem.name //получаем доступ к элементам
            // TODO: установить последнюю дату отмеченной привычки
            //tvTime.text = habitNameItem.lastCheckedDate.toString()

            //pBar.max = habitNameItem.allItemCounter//48 максимальный прогресс прогрессбара - это всего задач
            //pBar.progress = habitNameItem.checkedItemsCounter //48 на сколько мы продвинулись, сколько раз выполнили
            val colorState = ColorStateList.valueOf(getProgressColorState(habitNameItem, binding.root.context))
            pBar.progressTintList = colorState //48 tintList принимает colorState
            counterCard.backgroundTintList = colorState //48 цвет счетчика меняется в зависимости все или нет задачи выполнили

            val counterText = "${habitNameItem.lastWeekCheckCount}/${habitNameItem.targetWeekCheckCount}"//48 текст нужно составлять из разных частей, поэтому создадим отдельную переменную
            tvCounter.text = counterText

            // TODO: переходить на экран задач при нажатии на item
            /*itemView.setOnClickListener {
                listener.onClickItem(habitNameItem, NAME) //будет открываться список задач
            } //itemView - при нажатии открыть список*/

            // TODO: открывать диалог редактирования привычки при нажатии кнопки Редактировать
            ibEdit.setOnClickListener {
                listener.editItem(habitNameItem) //29 передается весь элемент
            }

            ibDelete.setOnClickListener {
                listener.deleteItem(habitNameItem.id) // возвращает от каждого элемента идентификатор
            }

            chBoxHabit.isChecked = habitNameItem.isChecked
            setPaintFlagAndColor(binding) //39 запускаем функцию перечеркивания текста один раз, когда обновляется адаптер

            chBoxHabit.setOnClickListener {
                if (chBoxHabit.isChecked) {
                    listener.saveToCheckedEntity(habitNameItem)
                } else {
                    listener.deleteFromCheckedEntity(habitNameItem)
                    //TODO: удалить строку из таблицы HabitCheckedItem
                    //listener.deleteFromCheckedEntity(habitCheckedItem.id!!)
                }
            }

                    // TODO: удалить данные из таблицы HabitCheckedItem
 /*                    listener.onClickItem (habitCheckedItem.copy(
                        habitChecked = chBoxHabit.isChecked,
                        checkedHabitCounter = habitNameItem.checkedHabitCounter - 1),
                        CHECK_BOX) //0322 записываем true или false*//*

                } // else - если непрочекано*/

        }
             // при нажатии на кнопку edit запускается listener и editItem интерфейс, который нужно добавить во фрагмент


        private fun getProgressColorState(item: MainViewModel.HabitItemModel, context: Context): Int {
            // TODO: Return old logic
            return ContextCompat.getColor(context, R.color.green_main)
            /*return if (item.checkedItemsCounter == item.allItemCounter) {
                ContextCompat.getColor(context, R.color.green_main) //48 если отмечены все элементы в списке, цвет зеленый
            } else {
                ContextCompat.getColor(context, R.color.red_main) //48 цвет прогрессбара красный
            } //48 если не все элементы отмечены*/

        }  // цвет для прогрессбара, нужно применить в setData


        private fun setPaintFlagAndColor(binding: HabitNameItemBinding) {
            binding.apply {

                if (chBoxHabit.isChecked) {
                    tvHabitName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG //перечеркнуть весь текст
                    // TODO: раскомментить, когда будет выведена дата последнего выполнения привычки
                    //tvTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvHabitName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light)) // изменение текста на серый при отметке чекбокса
                    //tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light))
                } else {
                    //tvTime.paintFlags = Paint.ANTI_ALIAS_FLAG // убрать перечеркивание названия Привычки
                    //tvTime.paintFlags = Paint.ANTI_ALIAS_FLAG // убрать перечеркивание времени создания Привычки
                    //tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black)) // изменение цвета на черный при снятии отметки чекбокса
                    //tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup): ItemHolder{
                return ItemHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.habit_name_item,
                    parent, false))
            }
        } // когда запускаем ItemHolder, Create выдает уже инициализированный класс holder, который хранит в себе ссылку на загруженную в память разметку

    } // ItemHolder - хранится ссылка на разметку и элементы


    class ItemComparator : DiffUtil.ItemCallback<MainViewModel.HabitItemModel>() {

        override fun areItemsTheSame(oldItem: MainViewModel.HabitItemModel, newItem: MainViewModel.HabitItemModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MainViewModel.HabitItemModel, newItem: MainViewModel.HabitItemModel): Boolean {
            return oldItem == newItem
        }
    } // diffUtil - сравнивает элементы из старого и нового списков


    interface Listener {
        fun deleteItem(id: Int) // функция определена во фрагменте
        fun editItem(habitNameItem: MainViewModel.HabitItemModel) //29 редактирование записи из БД. Передаем полностью название, потому что будем изменять
        fun onClickItem(habitNameItem: MainViewModel.HabitItemModel, state: Int)
        fun saveToCheckedEntity(habitNameItem: MainViewModel.HabitItemModel)
        fun deleteFromCheckedEntity(habitNameItem: MainViewModel.HabitItemModel)
    }


    companion object {
        const val NAME = 0
        const val CHECK_BOX = 1
    } // чтобы передавать только одну функцию в интерфейс и условием определять что сделать

}