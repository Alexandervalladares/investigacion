package com.example.todolist

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var listaLayout: LinearLayout
    private lateinit var editText: EditText
    private lateinit var prefs: android.content.SharedPreferences
    private val tareas = mutableListOf<Tarea>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("tareas", Context.MODE_PRIVATE)

        // Layout principal (vertical)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        editText = EditText(this).apply {
            hint = "Nueva tarea"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val botonAgregar = Button(this).apply {
            text = "Agregar"
            setOnClickListener { agregarTarea() }
        }
        listaLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        layout.addView(editText)
        layout.addView(botonAgregar)
        layout.addView(listaLayout)

        setContentView(layout)

        cargarTareas()
        mostrarTareas()
    }

    private fun agregarTarea() {
        val texto = editText.text.toString().trim()
        if (texto.isNotEmpty()) {
            tareas.add(Tarea(texto, false))
            editText.text.clear()
            guardarTareas()
            mostrarTareas()
        }
    }

    private fun mostrarTareas() {
        listaLayout.removeAllViews()
        tareas.forEachIndexed { i, tarea ->
            val fila = LinearLayout(this)
            fila.orientation = LinearLayout.HORIZONTAL

            val checkBox = CheckBox(this)
            checkBox.isChecked = tarea.completada
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                tarea.completada = isChecked
                guardarTareas()
                mostrarTareas()
            }

            val textoTarea = TextView(this)
            textoTarea.text = tarea.texto
            textoTarea.textSize = 18f
            if (tarea.completada) {
                textoTarea.paintFlags = textoTarea.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textoTarea.paintFlags = textoTarea.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            val botonEliminar = Button(this)
            botonEliminar.text = "Eliminar"
            botonEliminar.setOnClickListener {
                tareas.removeAt(i)
                guardarTareas()
                mostrarTareas()
            }

            fila.addView(checkBox)
            fila.addView(textoTarea)
            fila.addView(botonEliminar)
            listaLayout.addView(fila)
        }
    }

    private fun guardarTareas() {
        val texto = tareas.joinToString("\n") { "${if (it.completada) "1" else "0"}|${it.texto}" }
        prefs.edit().putString("listado", texto).apply()
    }

    private fun cargarTareas() {
        tareas.clear()
        val texto = prefs.getString("listado", "") ?: ""
        if (texto.isNotEmpty()) {
            texto.split("\n").forEach {
                val partes = it.split("|", limit = 2)
                if (partes.size == 2) tareas.add(Tarea(partes[1], partes[0] == "1"))
            }
        }
    }
}

data class Tarea(val texto: String, var completada: Boolean)