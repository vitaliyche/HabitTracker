package com.codeliner.habittracker.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.codeliner.habittracker.R
import com.codeliner.habittracker.databinding.ActivityNewNoteBinding
import com.codeliner.habittracker.entities.NoteItem
import com.codeliner.habittracker.fragments.NoteFragment
import com.codeliner.habittracker.utils.HtmlManager
import com.codeliner.habittracker.utils.MyTouchListener
import com.codeliner.habittracker.utils.TimeManager
import java.text.SimpleDateFormat
import java.util.*

class NewNoteActivity : AppCompatActivity() {
    private lateinit var defPref: SharedPreferences
    private lateinit var binding: ActivityNewNoteBinding
    private var note: NoteItem? = null
    private var pref: SharedPreferences? = null //52 переменная для Настроек

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewNoteBinding.inflate(layoutInflater)
        defPref = PreferenceManager.getDefaultSharedPreferences(this)//55 инициализация настроек
        setTheme(getSelectedTheme()) //55 чтобы тема обновилась, нужно запускать функцию перед setContentView, до выбора экрана
        setContentView(binding.root)
        actionBarSettings() //активация кнопки Назад
        getNote()
        init()
        setTextSize() //52 изменение размера текста инициализируем после init, иначе не сработает
        onClickColorPicker() //21 изменяем цвет текста
        actionMenuCallback() //22 стираем всплывающее меню
    }

    private fun onClickColorPicker() = with(binding) { //21 слушатель нажатий на colorPicker
        imRed.setOnClickListener {
            setColorForSelectedText(R.color.picker_red)
        }
        imBlack.setOnClickListener {
            setColorForSelectedText(R.color.picker_black)
        }
        imBlue.setOnClickListener {
            setColorForSelectedText(R.color.picker_blue)
        }
        imYellow.setOnClickListener {
            setColorForSelectedText(R.color.picker_yellow)
        }
        imGreen.setOnClickListener {
            setColorForSelectedText(R.color.picker_green)
        }
        imOrange.setOnClickListener {
            setColorForSelectedText(R.color.picker_orange)
        }
    }

    @SuppressLint("ClickableViewAccessibility") //добавили supress по подсказке
    private fun init() { //функция для перетаскивания colorPicker из MyTouchListener
        binding.colorPicker.setOnTouchListener(MyTouchListener())
        pref = PreferenceManager.getDefaultSharedPreferences(this) //52 инициализация PreferenceManager. this - это активити
    }

    private fun getNote() { //получаем заметку с помощью интента
        val sNote = intent.getSerializableExtra(NoteFragment.NEW_NOTE_KEY) //сделаем проверку, snote - serializable note
        if (sNote != null) { // функция запустится только если note не null
            note = sNote as NoteItem
            fillNote()
        }
    }

    private fun fillNote() = with(binding){ //значит надо заполнить поля существующей заметкой
            edTitle.setText(note?.title)
            edDescription.setText(HtmlManager.getFromHtml(note?.content!!).trim()) //getFromHtml - чтобы выдал spanned текст
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //слушатель нажатий, запускается из NoteFragment
        if (item.itemId == R.id.id_save) { //если нажата кнопка сохранить заметку
            setMainResult()
        } else if (item.itemId == android.R.id.home) { //если нажата стрелка назад (home), то активити просто закроется
            finish()
        } else if (item.itemId == R.id.id_bold) { // если нажата кнопка bold
            setBoldForSelectedText() //создаем функцию для выделения
        } else if (item.itemId == R.id.id_color) {
            if (binding.colorPicker.isShown) { //закрываем colorpicker, если показан
                closeColorPicker()
            } else { //открываем colorpicker, если не показан
                openColorPicker()
            }
        }
            return super.onOptionsItemSelected(item)
    }

    private fun setBoldForSelectedText() = with(binding) { // функция выделения текста жирным при создании заметки
        val startPos = edDescription.selectionStart //нужно понять откуда до куда выделять текст
        val endPos = edDescription.selectionEnd
        val styles = edDescription.text.getSpans(startPos, endPos, StyleSpan::class.java) //нужно проверить есть ли там уже стиль Болд
        var boldStyle: StyleSpan? = null //если жирный шрифт, то убрать. Если нет - то добавить
        if (styles.isNotEmpty()) { //если не пусто
            edDescription.text.removeSpan(styles[0]) //убираем стиль с нулевой позицией, так как там только bold может быть
        } else { //добавляем если не было ничего
            boldStyle = StyleSpan(Typeface.BOLD)
        }
        edDescription.text.setSpan(boldStyle, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // trim - удаляет все пробелы из html
        //также нужно указать trim в NoteAdapter
        edDescription.text.trim()
        //установить курсор в начало выделения после выполнения
        edDescription.setSelection(startPos)
    }

    //21 функция выделения текста цветом, запускаем когда жмем на какой-нибудь цвет из colorPicker
    private fun setColorForSelectedText(colorId: Int) = with(binding) {
        //нужно понять откуда до куда выделять текст
        val startPos = edDescription.selectionStart
        val endPos = edDescription.selectionEnd
        //21 нужно проверить есть ли там уже цвет. если есть - удаляем и ставим новый цвет
        val styles = edDescription.text.getSpans(startPos, endPos, ForegroundColorSpan::class.java)
        //21 если не пусто, удаляем цвет и добавляем новый
        if (styles.isNotEmpty()) edDescription.text.removeSpan(styles[0])
        edDescription.text.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(this@NewNoteActivity, colorId)),
                startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // trim - удаляет все пробелы из html
        //также нужно указать trim в NoteAdapter
        edDescription.text.trim()
        //установить курсор в начало выделения после выполнения
        edDescription.setSelection(startPos)
        //и нужно добавить слушатель нажатий
    }

    //функция для результата
    private fun setMainResult() {
        var editState = "new"

        val tempNote: NoteItem?
        // если создание новой заметки
        if (note == null) {
            tempNote = createNewNote()
        //если редактирование заметки
        } else {
            editState = "update"
            tempNote = updateNote()
        }
        val i = Intent().apply {
            //в putextra создаем и заполняем класс NoteItem
            putExtra(NoteFragment.NEW_NOTE_KEY, tempNote)
            putExtra(NoteFragment.EDIT_STATE_KEY, editState)
        }
        setResult(RESULT_OK, i)
        finish()
    }

    //выдает обновленную заметку
    private fun updateNote() : NoteItem? = with(binding) {
        return note?.copy(
            title = edTitle.text.toString(),
            content = HtmlManager.toHtml(edDescription.text)
        )
    }
    // дальше проверка в onOptionsItemSelected

    //создание готового NoteItem (заполнение заголовка, описания, времени, id и т.д.)
    //возвращает заполненный класс NoteItem, который передаем обратно в NoteFragment, откуда и запрашиваем
    private fun createNewNote(): NoteItem {
        //return - так как будет нужно вернуть NoteItem
        return NoteItem(
            //null - идентификатор генерируется автоматически
            null,
            binding.edTitle.text.toString(),
            HtmlManager.toHtml(binding.edDescription.text),
            //возвращаем заполненное время, когда нажали на кнопку Создать заметку
            TimeManager.getCurrentTime(),
            ""
        )
    }

    //создание кнопки Назад
    private fun actionBarSettings() {
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    //анимация для colorpicker из папки anim
    //запускается, когда нажимаем кнопку в меню (прописать в onOptionsItemSelected)
    private fun openColorPicker() {
        //делаем colorPicker видимым
        binding.colorPicker.visibility = View.VISIBLE
        //запускаем анимацию
        val openAnim = AnimationUtils.loadAnimation(this, R.anim.open_color_picker)
        //передаем анимацию в layout
        binding.colorPicker.startAnimation(openAnim)
    }

        private fun closeColorPicker() {
            val openAnim = AnimationUtils.loadAnimation(this, R.anim.close_color_picker)
            //включаем прослушиватель и имплементируем members
            openAnim.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(p0: Animation?) {

                }

                //когда анимация заканчивается, нужно сделать невидимым layout
                override fun onAnimationEnd(p0: Animation?) {
                    binding.colorPicker.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }
            })
            binding.colorPicker.startAnimation(openAnim)
        }

    //22 функция, чтобы убирать всплывающее меню
    //плохо работает, выделяет текст только с 3го раза
    private fun actionMenuCallback() {
        //имплементируем функции ActionMode
        val actionCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                //22 очищаем всплывающее меню
                p1?.clear()
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                //22 очищаем всплывающее меню
                p1?.clear()
                return true
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {

            }
        }
        //нужно callback передать в editText и указать, что за меню будет следить actionCallback
        binding.edDescription.customSelectionActionModeCallback = actionCallback
    }

    private fun setTextSize() = with(binding) { //52 функция выбора размера текста
        edTitle.setTextSize(pref?.getString("title_size_key", "16")) //52 title_size_key - ключ соответствующей разметки в settings_preference
        edDescription.setTextSize(pref?.getString("content_size_key", "14"))
    } //52 инициализируем функцию в onCreate

    private fun EditText.setTextSize(size: String?) { //52 extension функция для EditText
        if(size != null) this.textSize = size.toFloat() //52 this - ссылаемся на EditText
    }

    private fun getSelectedTheme(): Int{ //55 функция выбора темы для экрана заметок
        return if (defPref.getString("theme_key", "blue") == "blue") {
            R.style.Theme_NewNoteBlue
        } else {
            R.style.Theme_NewNoteRed
        }
    }
}