package com.codeliner.habittracker.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.codeliner.habittracker.databinding.DeleteDialogBinding
import com.codeliner.habittracker.databinding.NewHabitDialogBinding

//23 object - чтобы запускался без инициализации
object DeleteDialog {
    //23 диалог запускаем при нажатии на кнопку New
    fun showDialog(context: Context, listener: Listener) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        //28 добавляем разметку в delete dialog
        val binding = DeleteDialogBinding.inflate(LayoutInflater.from(context))
        //23 указать, что билдер будет использовать разметку байндинг, иначе она не будет использоваться
        builder.setView(binding.root)
        //23 присваиваем слушатель нажатий на нашу кнопку
        binding.apply {
            bDelete.setOnClickListener {
                listener.onClick()
                dialog?.dismiss()
            }
            //28 cancel - просто закроем диалог и ничего не произойдет
            bCancel.setOnClickListener {
                dialog?.dismiss()
            }
        }
        dialog = builder.create()
        //23 чтобы убрать лишнее стандартное окно диалога
        dialog.window?.setBackgroundDrawable(null)
        //23 показываем наш диалог
        dialog.show()

    }
    interface Listener {
        fun onClick()
    }
}