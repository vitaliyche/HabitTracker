package com.codeliner.habittracker.activities

import android.app.Application
import com.codeliner.habittracker.db.MainDataBase

class MainApp : Application() {
    val database by lazy { MainDataBase.getDataBase(this) }
}