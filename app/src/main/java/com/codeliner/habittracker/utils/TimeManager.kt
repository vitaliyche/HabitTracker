package com.codeliner.habittracker.utils

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object TimeManager {
    const val DEF_TIME_FORMAT = "hh:mm:ss - yyyy/MM/dd" //53 формат времени по умолчанию
    fun getCurrentTime(): String { //получаем текущее время, которое установлено на смартфоне
        val formatter = SimpleDateFormat(DEF_TIME_FORMAT, Locale.getDefault()) //указываем формат времени и страну
        return formatter.format(Calendar.getInstance().time) //заменяем символы на реальное время, время берем из календаря
    }

    fun getTimeFormat(time: String, defPreferences: SharedPreferences): String{ //53 меняем последовательность данных времени
        val defFormatter = SimpleDateFormat(DEF_TIME_FORMAT, Locale.getDefault()) //53 преобразовали время из String в Calendar формат
        val defDate = defFormatter.parse(time)
        val newFormat = defPreferences.getString("time_format_key", DEF_TIME_FORMAT)
        val newFormatter = SimpleDateFormat(newFormat, Locale.getDefault())
        return if (defDate != null) {
            newFormatter.format(defDate) //53 время будет форматироваться в новый формат
        } else { //53 иначе просто возвращаем время, которое было
            time
        }
    }
}