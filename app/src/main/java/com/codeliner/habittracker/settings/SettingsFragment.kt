package com.codeliner.habittracker.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.codeliner.habittracker.R
import com.codeliner.habittracker.billing.BillingManager

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var removeAdsPref: Preference
    private lateinit var bManager: BillingManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
        init()
    }

    private fun init() { //60 инициализируем слушатель нажатий
        bManager = BillingManager(activity as AppCompatActivity)
        removeAdsPref = findPreference("remove_ads_key")!!
        removeAdsPref.setOnPreferenceClickListener {
            bManager.startConnection() //60 запускаем подключение, выходит диалог, чтобы пользователь мог оплатить
            true
        }
    } //60 запускаем из onCreatePreferences

    override fun onDestroy() { //60 закрываем подключение
        bManager.closeConnection()
        super.onDestroy()
    }

} //51 фрагмент запуститсяя из SettingsActivity