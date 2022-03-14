package com.codeliner.habittracker.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.NewHabitDialogBinding

object NewHabitDialog { //23 object - чтобы запускался без инициализации
    fun showDialog(context: Context, listener: Listener, name: String) { //23 диалог запускаем при нажатии на кнопку New //29 добавляем name, чтобы в одной функции было New и Edit
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val binding = NewHabitDialogBinding.inflate(LayoutInflater.from(context)) //23 в binding будут все наши элементы: editText и кнопка
        builder.setView(binding.root) //23 указать, что билдер будет использовать разметку байндинг, иначе она не будет использоваться
        binding.apply { //23 присваиваем слушатель нажатий на нашу кнопку
            edNewHabitName.setText(name) //29 если name пустой (New), то передается пустота. Иначе(edit) - передается name //чтобы при нажатии edit поле было не пустым
            if (name.isNotEmpty()) bCreate.text = context.getString(R.string.update) //29 если имя не пустое, значит пишем Обновить (вместо Создать)
            if (name.isNotEmpty()) tvTitleNewHabitDialog.text = context.getString(R.string.edit_habit_strings)
            bCreate.setOnClickListener {
                val habitName = edNewHabitName.text.toString() //23 проверяем, что текст habit name не пустой
                if (habitName.isNotEmpty()) {
                    listener.onClick(habitName) //23 запускаем интерфейс, который передает название на MainActivity //23 и уже в мэйн активити записываем через вьюмодел класс в интерфейс дао и в БД
                }
                dialog?.dismiss()
            }
        }
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(null) //23 чтобы убрать лишнее стандартное окно диалога
        dialog.show() //23 показываем наш диалог

    }
    interface Listener {
        fun onClick(name: String)
    }
}