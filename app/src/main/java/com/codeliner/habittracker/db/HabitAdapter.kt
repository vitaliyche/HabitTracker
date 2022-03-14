package com.codeliner.habittracker.db

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeliner.habittracker.R
import com.codeliner.habittracker.activities.HabitActivity
import com.codeliner.habittracker.databinding.HabitNameItemBinding
import com.codeliner.habittracker.entities.HabitNameItem

//diffUtil сравнивает элементы из старого и нового списков
//добавили listener, который нужно передать в setData
class HabitAdapter(private val listener: Listener): ListAdapter<HabitNameItem, HabitAdapter.ItemHolder>(ItemComparator()) {

    //создаем viewHolder
    // когда мы запустим адаптер на нашем активити, то сюда передаст ViewGroup
    // функция для каждой заметки из базы данных будет создавать собственный itemholder
    //который в себе будет создавать разметку для каждого элемента
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    // заполнение разметки сразу после создания
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        //интерфейс один и тот же добавится на все слушатели из списка
        //28 listener присваивается каждому элементу
        holder.setData(getItem(position), listener)
    }

    //в ItemHolder хранится ссылка на разметку и элементы
    class ItemHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = HabitNameItemBinding.bind(view)

        fun setData(habitNameItem: HabitNameItem, listener: Listener) = with(binding) { // с помощью setdata можем заполнять разметку
            tvHabitName.text = habitNameItem.name //получаем доступ к элементам
            tvTime.text = habitNameItem.time
            pBar.max = habitNameItem.allItemCounter//48 максимальный прогресс прогрессбара - это всего задач
            pBar.progress = habitNameItem.checkedItemsCounter //48 на сколько мы продвинулись, сколько задач выполнили
            val colorState = ColorStateList.valueOf(getProgressColorState(habitNameItem, binding.root.context))
            pBar.progressTintList = colorState //48 tintList принимает colorState
            counterCard.backgroundTintList = colorState //48 цвет счетчика меняется в зависимости все или нет задачи выполнили
            val counterText = "${habitNameItem.checkedItemsCounter}/${habitNameItem.allItemCounter}"//48 текст нужно составлять из разных частей, поэтому создадим отдельную переменную
            tvCounter.text = counterText
            itemView.setOnClickListener { //itemView - при нажатии будем открывать наш список
                listener.onClickItem(habitNameItem)
            }
            //28 если нажмем на кнопку Удалить, то запускается слушатель,
            //28 который возвращает от каждого элемента идентификатор
            //28 и по этому идентификатору мы можем удалить из БД данный элемент
            ibDelete.setOnClickListener{
                listener.deleteItem(habitNameItem.id!!)
            }
            //29 при нажатии на кнопку edit запускается listener
            // и editItem интерфейс, который нужно добавить во фрагмент
            ibEdit.setOnClickListener{
                listener.editItem(habitNameItem) //29 передается весь элемент
            }
        }

        private fun getProgressColorState(item: HabitNameItem, context: Context): Int { //48 цвет для прогрессбара
            return if (item.checkedItemsCounter == item.allItemCounter) { //48 если отмечены все элементы в списке
                ContextCompat.getColor(context, R.color.green_main) //48 цвет зеленый
            } else { //48 если не все элементы отмечены
                ContextCompat.getColor(context, R.color.red_main) //48 цвет прогрессбара красный
            }
        } //48 цвет нужно применить в setData

        // когда запускаем ItemHolder,
        // Create выдает уже инициализированный класс holder,
        // который хранит в себе ссылку на загруженную в память разметку
        companion object{
            //инициализируем ItemHolder
            fun create(parent: ViewGroup): ItemHolder{
                //возвращаем ItemHolder с загруженной разметкой
                return ItemHolder(
                    //надуваем разметку
                    LayoutInflater.from(parent.context).
                    inflate(R.layout.habit_name_item, parent, false))
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<HabitNameItem>() {

        //сравнивает похожи ли элементы
        override fun areItemsTheSame(oldItem: HabitNameItem, newItem: HabitNameItem): Boolean {
            //сравниваем идентификаторы, чтобы узнать это один и тот же item
            // и нужно ли обновлять список
            return oldItem.id == newItem.id
        }

        //сравнивает весь контент внутри элемента
        override fun areContentsTheSame(oldItem: HabitNameItem, newItem: HabitNameItem): Boolean {
            return oldItem == newItem
        }
    }

    interface Listener {
        //удаление записи из БД
        fun deleteItem(id: Int)
        //29 редактирование записи из БД. Передаем полностью название, потому что будем изменять
        fun editItem(habitNameItem: HabitNameItem)

        fun onClickItem(habitNameItem: HabitNameItem)

    }
}