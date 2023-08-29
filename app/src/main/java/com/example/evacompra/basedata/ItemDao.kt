package com.example.EvaCompra

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// Definición del Data Access Object (DAO) para la entidad Item
@Dao // Anotación que indica que esta interfaz es un DAO (Data Access Object)
interface ItemDao {
    // Consulta para obtener todos los items ordenados por si están comprados y luego por nombre
    @Query("SELECT * FROM item ORDER BY comprada ASC, nombre ASC")
    fun getAllItems(): List<Item>

    // Consulta para contar todos los items en la base de datos
    @Query("SELECT COUNT(*) FROM item")
    fun countAll(): Int

    // Consulta para eliminar todos los items de la base de datos
    @Query("DELETE FROM item")
    suspend fun deleteAll()

    // Inserción de un nuevo item en la base de datos
    @Insert
    fun insertItem(item: Item): Long

    // Actualización de un item existente en la base de datos
    @Update
    fun updateItem(item: Item)

    // Eliminación de un item de la base de datos
    @Delete
    fun deleteItem(item: Item)
}
