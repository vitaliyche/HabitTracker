package com.codeliner.habittracker.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.codeliner.habittracker.R

class SettingsActivity : AppCompatActivity() { //51 после нажатия на кнопку Settings, запускается SettingsActivity
    private lateinit var defPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) { //51 внутри активити вставляем SettingsFragment с экрана Preference
        super.onCreate(savedInstanceState)
        defPref = PreferenceManager.getDefaultSharedPreferences(this)//55 инициализация настроек
        setTheme(getSelectedTheme()) //55 чтобы тема обновилась, нужно запускать функцию перед setContentView, до выбора экрана
        setContentView(R.layout.activity_settings)
        if(savedInstanceState == null) { //51 если равно null
            supportFragmentManager.beginTransaction().replace(R.id.placeHolder, SettingsFragment()).commit()//51 тогда заново запускаем наш фрагмент, который появится в активити
        } //51 если не равно null, значит наш фрагмент уже открыт и не нужно это делать
        supportActionBar?.setDisplayHomeAsUpEnabled(true)    //51 добавляем стрелку назад
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //51 переход назад по нажатию стрелки Назад
        if (item.itemId == android.R.id.home) finish() //51 если нажата стрелка, закрываем активити
        return super.onOptionsItemSelected(item)
    }

    private fun getSelectedTheme(): Int{ //55 функция выбора темы
        return if (defPref.getString("theme_key", "blue") == "blue") {
            R.style.Theme_HabitTrackerBlue
        } else {
            R.style.Theme_HabitTrackerRed
        }
    }
} //51 нажатие на кноку Settings - в MainActivity