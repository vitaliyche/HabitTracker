package com.codeliner.habittracker.fragments

import androidx.appcompat.app.AppCompatActivity
import com.codeliner.habittracker.R

//переключение между фрагментами
object FragmentManager {
    //каждый раз при показе фрагмента на экране, он записывается в currentFrag
    // это позволяет не дублировать функции onClickNew
    // запускается в зависимости от того какой фрагмент подключен, не нужно знать что за фрагмент
    var currentFrag: BaseFragment? = null

    fun setFragment(newFrag: BaseFragment, activity: AppCompatActivity) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        //содержимое placeholder заменить на newfragment
        transaction.replace(R.id.placeholder, newFrag)
        transaction.commit()
        currentFrag = newFrag
    }
}