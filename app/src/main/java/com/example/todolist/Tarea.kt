package com.example.todolist

data class Tarea(
    var texto: String,
    var completada: Boolean,
    var fechaVencimiento: String? = null,
    var categoria: String? = null
)