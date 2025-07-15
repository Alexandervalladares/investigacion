package com.example.todolist

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: MutableList<Tarea>
    private lateinit var prefs: android.content.SharedPreferences
    private val categorias = listOf("Personal", "Trabajo", "Estudio", "Otro")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("tareas", Context.MODE_PRIVATE)
        tasks = mutableListOf()
        loadTasks()

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            tasks,
            onTaskChecked = { pos, checked -> updateTask(pos, checked) },
            onTaskDeleted = { pos -> deleteTask(pos) },
            onTaskEdit = { pos -> showEditTaskDialog(pos) }
        )
        recyclerView.adapter = adapter

        val fab: FloatingActionButton = findViewById(R.id.fabAddTask)
        fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun showAddTaskDialog() {
        showTaskDialog(null)
    }

    private fun showEditTaskDialog(pos: Int) {
        showTaskDialog(pos)
    }

    private fun showTaskDialog(editPos: Int?) {
        val isEdit = editPos != null
        val tarea = if (isEdit) tasks[editPos!!] else null

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val editText = view.findViewById<EditText>(R.id.editTextTask)
        val buttonPickDate = view.findViewById<Button>(R.id.buttonPickDate)
        val textViewSelectedDate = view.findViewById<TextView>(R.id.textViewSelectedDate)
        val editTextCategory = view.findViewById<AutoCompleteTextView>(R.id.editTextCategory)

        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        editTextCategory.setAdapter(adapterCategorias)

        var fechaSeleccionada: String? = tarea?.fechaVencimiento

        if (isEdit) {
            editText.setText(tarea?.texto)
            fechaSeleccionada?.let { textViewSelectedDate.text = it }
            editTextCategory.setText(tarea?.categoria ?: "", false)
        }

        buttonPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                fechaSeleccionada = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                textViewSelectedDate.text = fechaSeleccionada
            }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, listener, year, month, day).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Editar tarea" else "Nueva tarea")
            .setView(view)
            .setPositiveButton(if (isEdit) "Guardar" else "Agregar") { _, _ ->
                val text = editText.text.toString().trim()
                val categoria = editTextCategory.text.toString().trim().ifEmpty { null }
                val fecha = fechaSeleccionada
                if (text.isNotEmpty()) {
                    if (isEdit) {
                        // Edit
                        val t = tasks[editPos!!]
                        t.texto = text
                        t.fechaVencimiento = fecha
                        t.categoria = categoria
                        saveTasks()
                        adapter.notifyItemChanged(editPos)
                    } else {
                        // Nuevo
                        tasks.add(Tarea(text, false, fecha, categoria))
                        saveTasks()
                        adapter.notifyItemInserted(tasks.size - 1)
                    }
                } else {
                    Toast.makeText(this, "El texto no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateTask(pos: Int, checked: Boolean) {
        tasks[pos].completada = checked
        saveTasks()
        adapter.notifyItemChanged(pos)
    }

    private fun deleteTask(pos: Int) {
        if (pos >= 0 && pos < tasks.size) {
            tasks.removeAt(pos)
            saveTasks()
            adapter.notifyItemRemoved(pos)
        }
    }

    private fun saveTasks() {
        // Serializa los datos con separadores seguros
        val text = tasks.joinToString("\n") {
            "${if (it.completada) "1" else "0"}|${it.texto}|${it.fechaVencimiento.orEmpty()}|${it.categoria.orEmpty()}"
        }
        prefs.edit().putString("listado", text).apply()
    }

    private fun loadTasks() {
        tasks.clear()
        val text = prefs.getString("listado", "") ?: ""
        if (text.isNotEmpty()) {
            text.split("\n").forEach {
                val parts = it.split("|", limit = 4)
                if (parts.size >= 2) {
                    tasks.add(
                        Tarea(
                            texto = parts[1],
                            completada = parts[0] == "1",
                            fechaVencimiento = parts.getOrNull(2)?.ifEmpty { null },
                            categoria = parts.getOrNull(3)?.ifEmpty { null }
                        )
                    )
                }
            }
        }
    }
}