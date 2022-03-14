package com.codeliner.habittracker.db

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.NoteListItemBinding
import com.codeliner.habittracker.entities.NoteItem
import com.codeliner.habittracker.utils.HtmlManager
import com.codeliner.habittracker.utils.TimeManager

class NoteAdapter(private val listener: Listener, private val defPref: SharedPreferences): ListAdapter<NoteItem, NoteAdapter.ItemHolder>(ItemComparator()) { //diffUtil сравнивает элементы из старого и нового списков

    //создаем viewHolder
    // когда мы запустим адаптер на нашем активити, то сюда передаст ViewGroup
    // функция для каждой заметки из базы данных будет создавать собственный itemholder
    //который в себе будет создавать разметку для каждого элемента
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) { // заполнение разметки сразу после создания
        holder.setData(getItem(position), listener, defPref) //интерфейс один и тот же добавится на все слушатели из списка
    }

    class ItemHolder(view: View): RecyclerView.ViewHolder(view) { //в ItemHolder хранится ссылка на разметку и элементы
        private val binding = NoteListItemBinding.bind(view)

        fun setData(note: NoteItem, listener: Listener, defPref: SharedPreferences) = with(binding) { // с помощью setdata можем заполнять разметку
            tvTitle.text = note.title //получаем доступ к элементам
            tvDescription.text = HtmlManager.getFromHtml(note.content).trim() //передаем контент, получаем класс Spanned со стилем текста //trim - убирает лишние пробелы из html
            tvTime.text = TimeManager.getTimeFormat(note.time, defPref) //53 форматируем время через TimeManager, добавляя defPref
            itemView.setOnClickListener { //itemView - добавляем слушатель на весь элемент
                listener.onClickItem(note)
            }
            imDelete.setOnClickListener{ //добавляем слушатель нажатий на кнопку удаления,
                listener.deleteItem(note.id!!) //который срабатывает в NoteFragment
            } // когда запускаем ItemHolder, Create выдает уже инициализированный класс holder,
        } // который хранит в себе ссылку на загруженную в память разметку

        companion object{
            fun create(parent: ViewGroup): ItemHolder{ //инициализируем ItemHolder
                return ItemHolder( //возвращаем ItemHolder с загруженной разметкой
                    LayoutInflater.from(parent.context). //надуваем разметку
                    inflate(R.layout.note_list_item, parent, false))
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<NoteItem>() {

        override fun areItemsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean { //сравнивает похожи ли элементы
            return oldItem.id == newItem.id //сравниваем идентификаторы, чтобы узнать это один и тот же item
        } // и нужно ли обновлять список

        override fun areContentsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean { //сравнивает весь контент внутри элемента
            return oldItem == newItem
        }
    }

    interface Listener { //интерфейс запускаем в NoteFragment и передаем сюда
        fun deleteItem(id: Int) //удаление записи из БД
        fun onClickItem(note: NoteItem) //адаптер для редактирования заметки
    } // при нажатии передаем всю заметку
}