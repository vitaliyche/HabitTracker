package com.codeliner.habittracker.db

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.HabitLibraryItemBinding
import com.codeliner.habittracker.databinding.HabitNameItemBinding
import com.codeliner.habittracker.databinding.HabitTaskItemBinding
import com.codeliner.habittracker.entities.HabitTaskItem

//diffUtil сравнивает элементы из старого и нового списков
//добавили listener, который нужно передать в setData
class HabitTaskAdapter(private val listener: Listener): ListAdapter<HabitTaskItem, HabitTaskAdapter.ItemHolder>(ItemComparator()) {

    //создаем viewHolder
    // когда мы запустим адаптер на нашем активити, то сюда передаст ViewGroup
    // функция для каждой заметки из базы данных будет создавать собственный itemholder
    //который в себе будет создавать разметку для каждого элемента
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        //33 если 0, будем использовать нормальную разметку
        return if (viewType == 0)
            ItemHolder.createHabitItem(parent)
        else
            ItemHolder.createLibraryItem(parent)
    }

    // заполнение разметки сразу после создания
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        //интерфейс один и тот же добавится на все слушатели из списка
        //28 listener присваивается каждому элементу
        if (getItem(position).itemType == 0) {
            holder.setItemData(getItem(position), listener)
        } else {
            holder.setLibraryData(getItem(position), listener)
        }
    }

    //36 адаптер первым делом запускает эту функцию
    //33 возвращает viewType (0 или 1) в onCreateViewHolder
    //33 если записываем в БД элемент из библиотеки, то записываем 1, если элемент из списка - записываем 0
    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType //36 и берет поочереди из всех элементов списка itemtype
    } //36 передает type в oncreate view holder

    class ItemHolder(val view: View): RecyclerView.ViewHolder(view) { // в ItemHolder хранится ссылка на разметку и элементы

        fun setItemData(habitTaskItem: HabitTaskItem, listener: Listener) { //39 когда список обновляется, запускается setItemData
            val binding = HabitTaskItemBinding.bind(view)
            binding.apply {
                tvNameHLIt.text = habitTaskItem.name //36 заполняем название элемента
                tvInfoHTIt.text = habitTaskItem.itemInfo //37 что-то записываем
                tvInfoHTIt.visibility = infoVisibility(habitTaskItem) //37 либо спрячет элемент, либо покажет
                chBoxTaskHTIt.isChecked = habitTaskItem.itemChecked
                setPaintFlagAndColor(binding) //38 запускаем функцию перечеркивания текста
                chBoxTaskHTIt.setOnClickListener { //38 слушаем нажатие чекбокса выполнения задачи
                    listener.onClickItem(habitTaskItem.copy(itemChecked = chBoxTaskHTIt.isChecked), CHECK_BOX) //39 записываем true или false
                } //39 для сохранения состояния нужно сделать апдейт в БД Dao
                ibEditHTIt.setOnClickListener {//40 добавить слушатель нажатий на кнопку для редактирования задачи
                    listener.onClickItem(habitTaskItem, EDIT)//40 проверяем что делаем: редактирование или удаление
                }
            }
        }

        fun setLibraryData(habitTaskItem: HabitTaskItem, listener: Listener) { //заполнение подсказок нужными словами
            val binding = HabitLibraryItemBinding.bind(view) //45 взяли эту разметку
            binding.apply {
                tvNameHLIt.text = habitTaskItem.name
                ibEditHTIt.setOnClickListener {
                    listener.onClickItem(habitTaskItem, EDIT_LIBRARY_ITEM) //46 передастся через интерфейс в HabitActivity
                }
                ibDeleteHLIt.setOnClickListener {
                    listener.onClickItem(habitTaskItem, DELETE_LIBRARY_ITEM)
                }
                itemView.setOnClickListener { //47 itemView - нажатие на весь элемент
                    listener.onClickItem(habitTaskItem, ADD)
                }
            } //47 после нажатия на элемент, все запустится в HabitActivity
        }

        private fun setPaintFlagAndColor(binding: HabitTaskItemBinding) { //38 чтобы textView был перечеркнут
            binding.apply {
                if (chBoxTaskHTIt.isChecked) { //если чекбокс отмечен
                    tvNameHLIt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG //38 перечеркнуть весь текст
                    tvInfoHTIt.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvNameHLIt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light)) //38 изменение текста на серый при отметке чекбокса
                    tvInfoHTIt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light))
                } else { //если чекбокс не отмечен
                    tvNameHLIt.paintFlags = Paint.ANTI_ALIAS_FLAG //38 убрать перечеркивание названия задачи
                    tvInfoHTIt.paintFlags = Paint.ANTI_ALIAS_FLAG //38 убрать перечеркивание информации о задаче
                    tvNameHLIt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black)) //38 изменение цвета на черный при снятии отметки чекбокса
                    tvInfoHTIt.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                }
            } //38 запуск функции из setItemData
        }

        private fun infoVisibility(habitTaskItem: HabitTaskItem): Int { //37 функция проверяет показывать itemInfo или нет
            return if(habitTaskItem.itemInfo.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        companion object{ //33 в зависимости от того какую разметку хотим показать, будет создаваться разный ItemHolder
            fun createHabitItem(parent: ViewGroup): ItemHolder{ // инициализируем ItemHolder
                return ItemHolder( //возвращаем ItemHolder с загруженной разметкой
                    LayoutInflater.from(parent.context). //надуваем разметку
                    inflate(R.layout.habit_task_item, parent, false))
            }

            fun createLibraryItem(parent: ViewGroup): ItemHolder{ //33 если viewType 1, то используем функцию для библиотеки
                return ItemHolder(
                    LayoutInflater.from(parent.context).
                    inflate(R.layout.habit_library_item, parent, false))
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<HabitTaskItem>() {

        override fun areItemsTheSame(oldItem: HabitTaskItem, newItem: HabitTaskItem): Boolean { //сравнивает похожи ли элементы
            return oldItem.id == newItem.id //сравниваем идентификаторы, чтобы узнать это один и тот же item и нужно ли обновлять список
        }

        override fun areContentsTheSame(oldItem: HabitTaskItem, newItem: HabitTaskItem): Boolean { //сравнивает весь контент внутри элемента
            return oldItem == newItem
        }
    }

    interface Listener {
        fun onClickItem(habitTaskItem: HabitTaskItem, state: Int)

    }

    companion object { //40 чтобы передавать только одну функцию в нашем интерфейсе и с помощью условия определять что нужно сделать
        const val EDIT = 0
        const val CHECK_BOX = 1
        const val EDIT_LIBRARY_ITEM = 2
        const val DELETE_LIBRARY_ITEM = 3
        const val ADD = 4
    }
}