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
import java.util.*

class HabitAdapter(private val listener: Listener): ListAdapter<HabitNameItem, HabitAdapter.ItemHolder>(ItemComparator()) { //добавили listener, который нужно передать в setData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent) // функция для каждой заметки из базы данных будет создавать собственный itemholder
    } //создаем viewHolder // когда мы запустим адаптер на нашем активити, то сюда передаст ViewGroup, который в себе будет создавать разметку для каждого элемента

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener) //28 listener присваивается каждому элементу
    } // заполнение разметки сразу после создания //интерфейс один и тот же добавится на все слушатели из списка

    class ItemHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = HabitNameItemBinding.bind(view)

        fun setData(habitNameItem: HabitNameItem, listener: Listener) = with(binding) {

            tvHabitName.text = habitNameItem.name //получаем доступ к элементам
            tvTime.text = habitNameItem.time
            pBar.max = habitNameItem.allItemCounter//48 максимальный прогресс прогрессбара - это всего задач
            pBar.progress = habitNameItem.checkedItemsCounter //48 на сколько мы продвинулись, сколько раз выполнили
            val colorState = ColorStateList.valueOf(getProgressColorState(habitNameItem, binding.root.context))
            pBar.progressTintList = colorState //48 tintList принимает colorState
            counterCard.backgroundTintList = colorState //48 цвет счетчика меняется в зависимости все или нет задачи выполнили
            val counterText = "${habitNameItem.checkedHabitCounter}/${habitNameItem.planDaysPerWeek}"//48 текст нужно составлять из разных частей, поэтому создадим отдельную переменную
            tvCounter.text = counterText

            itemView.setOnClickListener { //itemView - при нажатии будем открывать наш список
                listener.onClickItem(habitNameItem, NAME) //220315 будет открываться список задач
            }

            ibDelete.setOnClickListener{ //28 если нажмем на кнопку Удалить, запускается слушатель,
                listener.deleteItem(habitNameItem.id!!) //28 который возвращает от каждого элемента идентификатор
            } //28 и по этому идентификатору мы можем удалить из БД данный элемент

            chBoxHabit.isChecked = habitNameItem.habitChecked
            setPaintFlagAndColor(binding) //39 запускаем функцию перечеркивания текста один раз, когда обновляется адаптер

            chBoxHabit.setOnClickListener { //0322 слушаем нажатие чекбокса выполнения привычки

                if(chBoxHabit.isChecked) {

                    listener.onClickItem (habitNameItem.copy(
                        habitChecked = chBoxHabit.isChecked,
                        checkedHabitCounter = habitNameItem.checkedHabitCounter + 1,
                        checkedHabitDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)), // DAY_OF_YEAR)-1 использовать для теста
                        CHECK_BOX) //0322 записываем true или false

                } else {
                    listener.onClickItem (habitNameItem.copy(
                        habitChecked = chBoxHabit.isChecked,
                        checkedHabitCounter = habitNameItem.checkedHabitCounter - 1),
                        CHECK_BOX) //0322 записываем true или false
                } //нужно проверить, что непрочекано, только тогда добавляем+1
            } //0322 для сохранения состояния нужно сделать апдейт в БД Dao

            ibEdit.setOnClickListener{ //29 при нажатии на кнопку edit запускается listener // и editItem интерфейс, который нужно добавить во фрагмент
                listener.editItem(habitNameItem) //29 передается весь элемент
            }

        } // SetData - с помощью setdata можем заполнять разметку


        // при нажатии на checked нужно добавить данные во вторую БД или удалить, если галочка убрана
        /*fun setHabitCheckedItem(habitCheckedItem: HabitCheckedItem, listener: Listener) = with(binding) {

            chBoxHabit.setOnClickListener { //0322 слушаем нажатие чекбокса выполнения привычки

                if(chBoxHabit.isChecked) {

                    listener.onClickItem (habitCheckedItem.copy(
                        habitId = tvHabitName.toString(),
                        checkedHabitDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)),
                        )
                }
            }
        }*/

        private fun getProgressColorState(item: HabitNameItem, context: Context): Int { //48 цвет для прогрессбара
            return if (item.checkedItemsCounter == item.allItemCounter) {
                ContextCompat.getColor(context, R.color.green_main) //48 если отмечены все элементы в списке, цвет зеленый
            } else {
                ContextCompat.getColor(context, R.color.red_main) //48 цвет прогрессбара красный
            } //48 если не все элементы отмечены
        } //48 цвет нужно применить в setData

        private fun setPaintFlagAndColor(binding: HabitNameItemBinding) { //38 чтобы textView был перечеркнут
            binding.apply {
                if (chBoxHabit.isChecked) { //если чекбокс отмечен
                    tvHabitName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG //38 перечеркнуть весь текст
                    tvTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvHabitName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light)) //38 изменение текста на серый при отметке чекбокса
                    tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light))
                } else { //если чекбокс не отмечен
                    tvTime.paintFlags = Paint.ANTI_ALIAS_FLAG //38 убрать перечеркивание названия Привычки
                    tvTime.paintFlags = Paint.ANTI_ALIAS_FLAG //38 убрать перечеркивание времени создания Привычки
                    tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black)) //38 изменение цвета на черный при снятии отметки чекбокса
                    tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                }
            } //38 запуск функции из setData
        }

        companion object{
            fun create(parent: ViewGroup): ItemHolder{ //инициализируем ItemHolder
                return ItemHolder( //возвращаем ItemHolder с загруженной разметкой
                    LayoutInflater.from(parent.context). //надуваем разметку
                    inflate(R.layout.habit_name_item, parent, false))
            }
        } // когда запускаем ItemHolder, Create выдает уже инициализированный класс holder, который хранит в себе ссылку на загруженную в память разметку
    } //в ItemHolder хранится ссылка на разметку и элементы

    class ItemComparator : DiffUtil.ItemCallback<HabitNameItem>() {

        override fun areItemsTheSame(oldItem: HabitNameItem, newItem: HabitNameItem): Boolean { //сравнивает похожи ли элементы
            return oldItem.id == newItem.id //сравниваем идентификаторы, чтобы узнать это один и тот же item // и нужно ли обновлять список
        }

        override fun areContentsTheSame(oldItem: HabitNameItem, newItem: HabitNameItem): Boolean { //сравнивает весь контент внутри элемента
            return oldItem == newItem
        }
    } //diffUtil сравнивает элементы из старого и нового списков

    interface Listener {
        fun deleteItem(id: Int) //удаление записи из БД
        fun editItem(habitNameItem: HabitNameItem) //29 редактирование записи из БД. Передаем полностью название, потому что будем изменять
        fun onClickItem(habitNameItem: HabitNameItem, state: Int)
    }

    companion object {
        const val NAME = 0
        const val CHECK_BOX = 1
    } //40 чтобы передавать только одну функцию в нашем интерфейсе и с помощью условия определять что нужно сделать
}