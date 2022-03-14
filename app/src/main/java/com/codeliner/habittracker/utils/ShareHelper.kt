package com.codeliner.habittracker.utils

import android.content.Intent
import com.codeliner.habittracker.entities.HabitTaskItem


object ShareHelper {
    fun shareTaskList(tasksList: List<HabitTaskItem>, habitName: String): Intent { //42 передаем список задач и название привычки
        val intent = Intent(Intent.ACTION_SEND) //42 укажем, что что-то хотим отправить
        intent.type = "text/plane" //42 будем передавать текст
        intent.apply {
            putExtra(Intent.EXTRA_TEXT, makeShareText(tasksList, habitName)) //42 мы хотим отправить сообщение
        }
        return intent // 42 эта функция должна вернуть интент подготовленный
    } //42 и мы в активити будем запускать ShareHelper

    private fun makeShareText(tasksList: List<HabitTaskItem>, habitName: String): String { //42 как будем передавать текст
        val sBuilder = StringBuilder() //42 что будем заполнять
        sBuilder.append("<<$habitName>>")//42 вверху появится заголовок
        sBuilder.append("\n")//42 чтобы все было не в одну строку, а мы перешли на следующую строку
        var counter = 0 //42 будем считать сколько задач у нас есть
        tasksList.forEach { //42 с помощью цикла пробегаем все элементы
            sBuilder.append("${++counter} - ${it.name} (${it.itemInfo})")//42 начинаем добавлять задачи
            sBuilder.append("\n") //42 после добавления элемента, делаем переход на следующую строку
        }
        return sBuilder.toString() //42 возвращаем наш текст
    } //42 текст будем помещать в наш интент
}