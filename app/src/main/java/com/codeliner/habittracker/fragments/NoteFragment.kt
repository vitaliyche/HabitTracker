package com.codeliner.habittracker.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.codeliner.habittracker.activities.MainApp
import com.codeliner.habittracker.activities.NewNoteActivity
import com.codeliner.habittracker.databinding.FragmentNoteBinding
import com.codeliner.habittracker.db.MainViewModel
import com.codeliner.habittracker.db.NoteAdapter
import com.codeliner.habittracker.entities.NoteItem

class NoteFragment : BaseFragment(), NoteAdapter.Listener {
    private lateinit var binding: FragmentNoteBinding
    private lateinit var editLauncher: ActivityResultLauncher<Intent>
    private lateinit var adapter: NoteAdapter //переменная, в которую будем записывать адаптер
    private lateinit var defPref: SharedPreferences
    private val mainViewModel: MainViewModel by activityViewModels { //в mainViewModel теперь есть allNotes, insertNote и т.д. из ViewModel
        MainViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database) //context превращаем в класс MainApp (инициализирующий приложение), в нем есть уже база данных
    }

    override fun onClickNew() { //все фрагменты, которые наследуются от BaseFragment, //должны иметь функцию onClickNew
        editLauncher.launch(Intent(activity, NewNoteActivity::class.java)) //при нажатии на кнопку Добавить новую заметку, //будет запускаться логика для добавления новой заметки в базу данных
    } //кнопку нужно запускать из FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) { //можем прослушивать и обновлять адаптер
        super.onCreate(savedInstanceState)
        onEditResult() //при создании активити запускаем ланчер и ждем результат
    }

    override fun onCreateView( //создание view, для управления заметками
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteBinding.inflate(inflater, container, false) // инициализируем FragmentNoteBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { //функция запускается, когда все view созданы,
        super.onViewCreated(view, savedInstanceState) //после чего можно инициализировать recyclerview
        initRcView()
        observer() //инициализация observer
    }

    private fun initRcView() = with(binding) { //инициализация recyclerView и адаптера //binding, чтобы напрямую использовать идентификатор
        defPref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        rcViewNote.layoutManager = getLayoutManager() //layoutManager - указываем вариант отображения заметок
        adapter = NoteAdapter(this@NoteFragment, defPref) //инициализация адаптера //this@NoteFragment - если просто this, то укажет на binding
        rcViewNote.adapter = adapter //передаем адаптер в rcViewNote //указываем адаптер, который будет обновлять recyclerView
    }

    private fun getLayoutManager(): RecyclerView.LayoutManager { //54 в зависимости от настроек будем получать нужный Layout Manager, чтобы передать его в наш Recycler View
        return if (defPref.getString("note_style_key", "Linear") == "Linear") { //54 если получили Linear
            LinearLayoutManager(activity)
        } else {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //54 в скобках указываем сколько столбцов
        }
    } //обновление настроек сделаем в Main Activity

    private fun observer() { //observer следит за изменениями в базе данных
        mainViewModel.allNotes.observe(viewLifecycleOwner) { //и будет выдавать каждый раз обновленный список
            adapter.submitList(it) //submitlist - обновляет адаптер
        }
    }

    private fun onEditResult() { //ждем результат с активити, который мы запустили, если придет результат
        editLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                //если мы отправили данные и вернулись с результатом
                if(it.resultCode == Activity.RESULT_OK) {
                    //проверка что происходит new note или update note
                    //передаем editState
                    val editState = it.data?.getStringExtra(EDIT_STATE_KEY)
                    //и проверяем
                    if (editState == "update") {
                        mainViewModel.updateNote(it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem)
                    } else {
                        //serializable - получаем как класс, а не просто байты данных
                        mainViewModel.insertNote(it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem)
                    }
                }
            }
    }

    override fun deleteItem(id: Int) { //функция, которая сработает, если запустим интерфейс NoteAdapter
        mainViewModel.deleteNote(id)
    }

    override fun onClickItem(note: NoteItem) { //при нажатии на заметку
        val intent = Intent(activity, NewNoteActivity::class.java).apply { //не просто запускаем, но и передаем заметку для редактирования
            putExtra(NEW_NOTE_KEY, note)
        }
        editLauncher.launch(intent)
    } //дальше идем в new note activity

    companion object { //чтобы была только одна инстанция фрагмента, если пытаемся запустить несколько раз
        const val NEW_NOTE_KEY = "new_note_key"
        const val EDIT_STATE_KEY = "edit_state_key"
        @JvmStatic
        fun newInstance() = NoteFragment()
    }
}