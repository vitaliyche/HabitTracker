package com.codeliner.habittracker.utils

import android.text.Html
import android.text.Spanned

//для сохранения в базу текста в виде html, для сохранения стиля
object HtmlManager {
    //превращаем текст в spanned - и передадим в editText
    fun getFromHtml(text: String): Spanned{
        //разные функции для разных версий андроид
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text)
        } else {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    fun toHtml(text: Spanned): String {
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            Html.toHtml(text)
        } else {
            Html.toHtml(text, Html.FROM_HTML_MODE_COMPACT)
        }
    }
    //дальше идем в NewNoteActivity
}