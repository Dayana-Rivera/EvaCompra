package com.example.EvaCompra

// Importaciones de librerías y componentes necesarios
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.res.Configuration
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.mispruebas.listacompra2.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

//..

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar una corutina para realizar operaciones en segundo plano
        lifecycleScope.launch(Dispatchers.IO) {
            // Obtener una instancia de la base de datos y su Data Access Object (DAO)
            val itemDao = DataApp.getInstance(this@MainActivity).itemDao()

            // Contar la cantidad de elementos en la base de datos
            val contarItem = itemDao.countAll()

            // Si no hay elementos, insertar algunos elementos de ejemplo
            if (contarItem < 1) {
                itemDao.insertItem(Item(0, "Leche", false))
                itemDao.insertItem(Item(0, "Pan", false))
                itemDao.insertItem(Item(0, "Huevos", false))
            }
        }

        // Establecer el contenido de la actividad con la UI definida en ListaComprasUI
        setContent {
            ListaComprasUI()
        }
    }

    // Manejar cambios en la configuración del dispositivo
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate() // Reiniciar la actividad para aplicar cambios en la configuración
    }
}

@Composable
fun ListaComprasUI() {
    // Declarar un CoroutineScope para manejar las corutinas
    val llamadaCoroutina = rememberCoroutineScope()

    // Obtener el contexto local
    val contexto = LocalContext.current

    // Estado para almacenar la lista de items
    val (items, setItems) = remember { mutableStateOf(emptyList<Item>()) }

    // Estado para controlar si mostrar el diálogo de agregar item
    val (mostrarDialogo, setMostrarDialogo) = remember { mutableStateOf(false) }

    // Estado para almacenar el nombre del nuevo item
    val (nombreItem, setNombreItem) = remember { mutableStateOf("") }

    // Efecto lanzado cuando cambia el estado de 'items'
    LaunchedEffect(items) {
        // Realizar operaciones en segundo plano con el uso de Dispatchers.IO
        withContext(Dispatchers.IO) {
            // Obtener el Data Access Object (DAO)
            val dao = DataApp.getInstance(contexto).itemDao()

            // Obtener y establecer la lista de items en el estado
            setItems(dao.getAllItems())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mostrar mensaje si no hay elementos en la lista
        if (items.isEmpty()) {
            Text(
                text = "No hay lista",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            // Mostrar la lista de items usando LazyColumn
            LazyColumn {
                items(items) { item ->
                    // Mostrar la interfaz de un elemento en la lista
                    ListaItemUI(item) {
                        setItems(emptyList()) // Actualizar la lista
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Mostrar botones para agregar y borrar elementos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón para mostrar el diálogo de agregar item
            Button(onClick = { setMostrarDialogo(true) }) {
                Text(text = stringResource(id = R.string.add_item))
            }

            // Botón para borrar todos los elementos
            Button(onClick = {
                llamadaCoroutina.launch(Dispatchers.IO) {
                    // Obtener el DAO y borrar todos los elementos
                    val dao = DataApp.getInstance(contexto).itemDao()
                    dao.deleteAll()
                    setItems(emptyList())
                }
            }) {
                Text(text = stringResource(id = R.string.delete_all))
            }
        }

        // Mostrar el diálogo para agregar un nuevo item si se activa 'mostrarDialogo'
        if (mostrarDialogo) {
            DialogoAgregarItem(nombreItem, setNombreItem) {
                llamadaCoroutina.launch(Dispatchers.IO) {
                    // Obtener el DAO e insertar un nuevo item
                    val dao = DataApp.getInstance(contexto).itemDao()
                    dao.insertItem(Item(0, nombreItem, false))
                    setItems(dao.getAllItems())
                    setMostrarDialogo(false) // Cerrar el diálogo después de agregar el item
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAgregarItem(
    nombreItem: String,
    setNombreItem: (String) -> Unit,
    agregarItem: () -> Unit
) {
    // Diálogo para agregar un nuevo item
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Agregar nuevo item") },
        text = {
            // Campo de texto para ingresar el nombre del nuevo item
            OutlinedTextField(
                value = nombreItem,
                onValueChange = { setNombreItem(it) },
                label = { Text("Nombre del item") },
                singleLine = true
            )
        },
        confirmButton = {
            // Botón para confirmar y agregar el item
            TextButton(
                onClick = {
                    if (nombreItem.isNotBlank()) {
                        agregarItem()
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            // Botón para cancelar el proceso de agregar item
            TextButton(onClick = {}) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ListaItemUI(item: Item, guardar: () -> Unit = {}) {
    val llamadaCoroutina = rememberCoroutineScope()
    val contexto = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .border(1.dp, Color.Gray)
            .background(Color.White)
    ) {
        val iconModifier = Modifier.clickable {
            llamadaCoroutina.launch(Dispatchers.IO) {
                val dao = DataApp.getInstance(contexto).itemDao()
                item.comprada = !item.comprada
                dao.updateItem(item)
                guardar()
            }
        }

        val icon = if (item.comprada) {
            Icons.Filled.Check
        } else {
            Icons.Filled.ShoppingCart
        }

        // Mostrar un icono interactivo para marcar como comprado o no comprado
        Icon(icon, contentDescription = "Icono", modifier = iconModifier)

        Spacer(modifier = Modifier.width(16.dp))

        // Mostrar el nombre del item
        Text(item.nombre, modifier = Modifier.weight(2f))

        // Mostrar un icono interactivo para eliminar el item
        Icon(
            Icons.Filled.Delete,
            contentDescription = "Eliminar",
            modifier = Modifier.clickable {
                llamadaCoroutina.launch(Dispatchers.IO) {
                    // Obtener el DAO y eliminar el item
                    val dao = DataApp.getInstance(contexto).itemDao()
                    dao.deleteItem(item)
                    guardar()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListaItemUIPreview() {
    // Ejemplos de items para previsualización
    Column {
        var item = Item(0, "Leche", false)
        ListaItemUI(item)

        var item2 = Item(0, "Pan", true)
        ListaItemUI(item2)

        var item3 = Item(0, "Huevos", false)
        ListaItemUI(item3)
    }
}
