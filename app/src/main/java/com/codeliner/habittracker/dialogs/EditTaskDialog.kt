package com.codeliner.habittracker.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.EditTaskDialogBinding
import com.codeliner.habittracker.databinding.NewHabitDialogBinding
import com.codeliner.habittracker.entities.HabitTaskItem

object EditTaskDialog {
    fun showDialog(context: Context, item: HabitTaskItem, listener: Listener) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val binding = EditTaskDialogBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)
        binding.apply {
            edNameEdTD.setText(item.name) //40 вставляем название задачи в диалог
            edInfoEdTD.setText(item.itemInfo)
            if (item.itemType == 1) { //46 если LibraryItem
                edInfoEdTD.visibility = View.GONE //46 то прятать строку info
            }
            bUpdateEdTD.setOnClickListener { //40 при нажатии на кнопку Update
                if (edNameEdTD.text.toString().isNotEmpty()) {//40 проверяем, что в editTextName не пусто
                    listener.onClick(item.copy(name = edNameEdTD.text.toString(), itemInfo = edInfoEdTD.text.toString())) //40 значит можем записывать
                }
                    dialog?.dismiss() //40 закрывается наш диалог
            }

        }
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(null)
        dialog.show()
    }
    interface Listener {
        fun onClick(item: HabitTaskItem)
    }
}