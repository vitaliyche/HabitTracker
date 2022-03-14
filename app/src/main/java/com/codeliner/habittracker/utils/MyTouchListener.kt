package com.codeliner.habittracker.utils

import android.view.MotionEvent
import android.view.View

//класс для перетаскивания colorPicker по экрану
class MyTouchListener : View.OnTouchListener {
    //сюда будем записывать позицию
    var xDelta = 0.0f
    var yDelta = 0.0f

    //функция имплементирована
    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        when(event?.action) {

            //когда мы отпустили элемент
            MotionEvent.ACTION_DOWN -> {
                //настоящая позиция минус позиция куда передвинули
                xDelta = v.x - event.rawX
                yDelta = v.y - event.rawY

            }

            //действие, когда схватили и движем объект
            MotionEvent.ACTION_MOVE -> {
                v.x = xDelta +event.rawX
                v.y = yDelta +event.rawY
            }
        }
        return true
        //теперь слушатель нужно добавить к нашему элементу view в NewNoteActivity
    }
}