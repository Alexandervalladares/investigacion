package com.example.todolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: MutableList<Tarea>,
    private val onTaskChecked: (Int, Boolean) -> Unit,
    private val onTaskDeleted: (Int) -> Unit,
    private val onTaskEdit: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        val textView: TextView = itemView.findViewById(R.id.textViewTask)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit)
        val textViewDueDate: TextView = itemView.findViewById(R.id.textViewDueDate)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Limpiar listeners previos
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.completada
        holder.textView.text = task.texto

        holder.textView.paintFlags = if (task.completada)
            holder.textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                if (task.completada != isChecked) onTaskChecked(pos, isChecked)
            }
        }
        holder.buttonDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onTaskDeleted(pos)
            }
        }
        holder.buttonEdit.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onTaskEdit(pos)
            }
        }

        // Fecha
        if (!task.fechaVencimiento.isNullOrEmpty()) {
            holder.textViewDueDate.visibility = View.VISIBLE
            holder.textViewDueDate.text = "Vence: ${task.fechaVencimiento}"
        } else {
            holder.textViewDueDate.visibility = View.GONE
        }

        // Categoría
        if (!task.categoria.isNullOrEmpty()) {
            holder.textViewCategory.visibility = View.VISIBLE
            holder.textViewCategory.text = "Categoría: ${task.categoria}"
        } else {
            holder.textViewCategory.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = tasks.size
}