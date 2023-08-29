package com.example.EvaCompra

// Importaciones necesarias
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Anotación @Database para definir la base de datos y sus propiedades
@Database(entities = [Item::class], version = 1)
abstract class DataApp : RoomDatabase() {

    // Declaración de una función abstracta para obtener el DAO de Item
    abstract fun itemDao(): ItemDao

    companion object {

        // Propiedad volátil que asegura que el acceso sea siempre desde la memoria principal
        @Volatile
        private var INSTANCE: DataApp? = null

        // Función para obtener la instancia de la base de datos (singleton pattern)
        fun getInstance(context: Context): DataApp {
            // Si la instancia ya existe, se devuelve esa instancia
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataApp::class.java,
                    "ItemsDB.db"
                )
                    .fallbackToDestructiveMigration() // Permite la migración destructiva (borrar y recrear la BD)
                    .build() // Construye la base de datos
                INSTANCE = instance // Se asigna la instancia para futuros accesos
                instance // Se devuelve la instancia
            }
        }
    }
}
