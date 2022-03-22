package com.codeliner.habittracker.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import com.codeliner.habittracker.R
import com.codeliner.habittracker.billing.BillingManager
import com.codeliner.habittracker.databinding.ActivityMainBinding
import com.codeliner.habittracker.dialogs.NewHabitDialog
import com.codeliner.habittracker.fragments.FragmentManager
import com.codeliner.habittracker.fragments.HabitNamesFragment
import com.codeliner.habittracker.fragments.NoteFragment
import com.codeliner.habittracker.settings.SettingsActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : AppCompatActivity(), NewHabitDialog.Listener {
    lateinit var binding: ActivityMainBinding
    private lateinit var defPref: SharedPreferences
    private var currentMenuItemId = R.id.habits_list //54 это id нижнего меню, по умолчанию habits_list
    private var currentTheme = ""
    private var iAd: InterstitialAd? = null //57 переменная, куда сохраняем рекламу, когда она уже загружена
    private var adShowCounter = 0 //57 счетчик нажатий для показа Interstitial рекламы
    private var adShowCounterMax = 2 //57 количество нажатий для показа Interstitial рекламы
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        defPref = PreferenceManager.getDefaultSharedPreferences(this)//55 инициализация настроек
        setTheme(getSelectedTheme()) //55 чтобы тема обновилась, нужно запускать функцию перед super.onCreate, до выбора экрана

        super.onCreate(savedInstanceState)
        pref = getSharedPreferences(BillingManager.MAIN_PREF, MODE_PRIVATE) //61 константа MODE_PRIVATE уже есть в активити, поэтому пишем напрямую, без контекста
        binding = ActivityMainBinding.inflate(layoutInflater) //инициализация binding, подключение разметки к активити
        setContentView(binding.root)
        currentTheme = defPref.getString("theme_key", "blue").toString() //56 как только создается активити, мы уже знаем какая тема выбрана, проверяем на отличие в onResume
        FragmentManager.setFragment(HabitNamesFragment.newInstance(), this) //24 фрагмент, который отображается при запуске приложения
        setBottomNavListener() //запуск функции
        if (!pref.getBoolean(BillingManager.REMOVE_ADS_KEY, false)) loadInterAd() //61 если записано false (нет покупки), то реклама запускается
    }

    private fun loadInterAd() {
        val request = AdRequest.Builder().build() //57 создали запрос на получение рекламы
        InterstitialAd.load(this, getString(R.string.inter_ad_id), request,
            object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(ad: InterstitialAd) { //57 когда успешно загрузилось объявление
                    iAd = ad
                }
                override fun onAdFailedToLoad(ad: LoadAdError) { //57 когда объявление не загрузилось
                    iAd = null
                }
        })
    }

    private fun showInterAd(adListener: AdListener) {
        if (iAd != null  && adShowCounter > adShowCounterMax && !pref.getBoolean(BillingManager.REMOVE_ADS_KEY, false)) { //57 если объявление загрузилось или количество нажатий больше заданного
            iAd?.fullScreenContentCallback = object : FullScreenContentCallback() { //fullScreenContentCallback - слушатель на каком этапе объявление
                override fun onAdDismissedFullScreenContent() { //57 когда user закрывает объявление //57 его перебрасывает в Настройки SettingsActivity
                    iAd = null //57 обнуляем объявление, потому что уже просмотрено
                    loadInterAd() //57 загружаем новое объявление
                    adListener.onFinish()
                }

                override fun onAdFailedToShowFullScreenContent(ad: AdError) { //57 если произошла ошибка
                    iAd = null //57 обнуляем объявление с ошибкой
                    loadInterAd() //57 загружаем новое объявление
                }

                override fun onAdShowedFullScreenContent() { //57 когда полностью объявление было показано
                    iAd = null //57 обнуляем объявление, потому что уже просмотрено
                    loadInterAd() //57 загружаем новое объявление
                }
            }

            adShowCounter = 0 //при показе объявления обнуляем счетчик показа рекламы после N нажатий на меню
            iAd?.show(this)//57 с помощью show показываем объявление
        } else { //57 если объявление НЕ загрузилось
            adShowCounter++ //57 считаем количество нажатий
            adListener.onFinish() //57 перенаправляем пользователя на нужный ему экран
        }
    }

    private fun setBottomNavListener() { //подключаем слушатель нажатий
        binding.bNav.setOnItemSelectedListener {
            when(it.itemId) { // проверяем на какую кнопку нажали
                R.id.settings -> { //когда жму на кнопку Settings
                    showInterAd(object : AdListener {
                        override fun onFinish() { //57 onFinish сработает только когда объявление закрыли или не загрузилось
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) //открывается экран с настройками, только когда запускается onFinish
                        }
                    })
                }
                R.id.notes -> {
                    showInterAd(object : AdListener {
                        override fun onFinish() { //57 onFinish сработает только когда объявление закрыли или не загрузилось
                            currentMenuItemId = R.id.notes //54 сохранено, что было выбрано, когда зашли в Настройки
                            FragmentManager.setFragment(NoteFragment.newInstance(), this@MainActivity) //при запуске должен появиться фрагмент NotFragment
                        }
                    })
                }
                R.id.habits_list -> { //если выбран habitNamesFragment и мы нажмем на кнопку New,
                    currentMenuItemId = R.id.habits_list //54 сохранено, что было выбрано, когда зашли в Настройки
                    FragmentManager.setFragment(HabitNamesFragment.newInstance(), this) //то запустится onClickNew во фрагменте HabitNamesFragment
                }
                R.id.new_item -> {
                    FragmentManager.currentFrag?.onClickNew()
                } //NewHabitDialog.showDialog(this, this)
            }
            true
        }
    }

    override fun onResume() { //54 когда возвращаемся с активити Настроек
        super.onResume()
        binding.bNav.selectedItemId = currentMenuItemId //54 какая кнопка должна быть нажата, когда возвращаемся
        if (defPref.getString("theme_key", "blue") != currentTheme) //56 если тема изменилась
            recreate() // //56 recreate пересоздает активити для моментального обновления темы
    } //54 записываем currentMenuItemId в setBottomNavListener

    private fun getSelectedTheme(): Int{ //55 функция выбора темы
        return if (defPref.getString("theme_key", "blue") == "blue") {
            R.style.Theme_HabitTrackerBlue
        } else {
            R.style.Theme_HabitTrackerRed
        }
    } //55 эту же функцию повторить в SettingsActivity, чтобы тема обновлялась и на экране настроек

    override fun onClick(name: String, days: String) { //23 имплементировали функцию, запускается после нажатия кнопки в диалоге
        Log.d("mylog", "Name: $name, Days: $days") //23 в этой функции онклик нужно будет сохранять в БД эту привычку,
    } //23 чтобы позже появлялся recycler view, появлялись все названия привычек в виде item-элементов

    interface AdListener {
        fun onFinish() //57 объявление было просмотрено
    } //57 интерфейс передаем в функцию showInterAd
}