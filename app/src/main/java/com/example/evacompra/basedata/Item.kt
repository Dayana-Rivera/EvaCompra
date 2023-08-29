package com.example.EvaCompra
import androidx.room.Entity
import androidx.room.PrimaryKey

// Definición de la entidad Item para ser utilizada por Room
@Entity(tableName = "item") // Anotación que indica que esta clase es una entidad y se almacenará en una tabla llamada "item"
data class Item(
    @PrimaryKey(autoGenerate = true) // Anotación que marca el campo 'id' como clave primaria y auto-generada
    val id: Int = 0, // Campo 'id' que es la clave primaria (auto-generada)
    var nombre: String, // Campo 'nombre' para almacenar el nombre del item
    var comprada: Boolean // Campo 'comprada' para almacenar si el item fue comprado o no
)
