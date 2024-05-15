package com.skye.petmath

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SharedViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _datosUsuario = MutableLiveData<List<DatosUsuario>>() // LiveData para almacenar los datos de usuario
    val datosUsuario: LiveData<List<DatosUsuario>> get() = _datosUsuario // LiveData público para observar los datos de usuario
    private val _nivel = MutableLiveData<Int>()
    val nivel: LiveData<Int> get() = _nivel


    init {
        // Llamar a la función para cargar los datos de usuario cuando se inicializa el ViewModel
        cargarDatosUsuario()
    }
//    fun actualizarNivel(nuevoNivel: Int, nuevaBarraNivel: Int) {
//        actualizarDatoUsuario("niv", nuevoNivel.toString())
//        actualizarDatoUsuario("barraniv", nuevaBarraNivel.toString())
//        cargarDatosUsuario()
//    }
//    fun actualizarNivel(nuevaBarraNivel: Int) {
//        actualizarDatoUsuario("barraniv", nuevaBarraNivel.toString())
//        cargarDatosUsuario()
//    }




    fun cargarDatosUsuario() {
        firestore.collection("infouser")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    // Manejar errores de consulta
                    return@addSnapshotListener
                }

                val listaDatosUsuario = mutableListOf<DatosUsuario>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        // Mapear los datos del documento a un objeto DatosUsuario
                        val datosUsuario = documento.toObject(DatosUsuario::class.java)
                        if (datosUsuario != null) {
                            listaDatosUsuario.add(datosUsuario)
                        }
                    }
                }
                _datosUsuario.value = listaDatosUsuario // Actualizar el LiveData con los datos obtenidos
            }
    }

    fun actualizarNumeroMonedas(cantidad: Int) {
        datosUsuario.value?.let { listaDatosUsuario ->
            if (listaDatosUsuario.isNotEmpty()) {
                val datosUsuario = listaDatosUsuario[0]
                val numeroMonedas = datosUsuario.moneda?.toInt() ?: 0
                val nuevaCantidadMonedas = numeroMonedas + cantidad

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val nuevoDatosUsuario = hashMapOf(
                        "moneda" to nuevaCantidadMonedas.toString() // Actualizar el número de monedas
                    )

                    firestore.collection("infouser").document(uid)
                        .update(nuevoDatosUsuario as Map<String, Any>)
                        .addOnSuccessListener {
                            // Éxito al actualizar los datos en Firestore
                            // Actualizar el LiveData después de la actualización
                            cargarDatosUsuario()
                        }
                        .addOnFailureListener { e ->
                            // Manejar errores al actualizar los datos en Firestore
                            //Toast.makeText(context, "Error al actualizar las monedas.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

    }
    fun actualizarNumeroMonedas1(cantidad: Int) {
        datosUsuario.value?.let { listaDatosUsuario ->
            if (listaDatosUsuario.isNotEmpty()) {
                val datosUsuario = listaDatosUsuario[0]
                val numeroMonedas = datosUsuario.moneda?.toInt() ?: 0
                val nuevaCantidadMonedas = numeroMonedas - cantidad

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val nuevoDatosUsuario = hashMapOf(
                        "moneda" to nuevaCantidadMonedas.toString() // Actualizar el número de monedas
                    )

                    firestore.collection("infouser").document(uid)
                        .update(nuevoDatosUsuario as Map<String, Any>)
                        .addOnSuccessListener {
                            // Éxito al actualizar los datos en Firestore
                            // Actualizar el LiveData después de la actualización
                            cargarDatosUsuario()
                        }
                        .addOnFailureListener { e ->
                            // Manejar errores al actualizar los datos en Firestore
                            //Toast.makeText(context, "Error al actualizar las monedas.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

    }


    fun actualizarNivel(nivel: Int, barraNivel: Int) {
        // Verificar si la barra de nivel llegó a 100
        if (barraNivel == 100) {
            // Actualizar en la base de datos solo si la barra de nivel llega a 100
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val nuevoDatosUsuario = hashMapOf(
                    "niv" to nivel,
                    "barraniv" to 0 // Reiniciar la barra de nivel
                )

                FirebaseFirestore.getInstance().collection("infouser").document(uid)
                    .update(nuevoDatosUsuario as Map<String, Any>)
                    .addOnSuccessListener {
                        // Éxito al actualizar los datos en Firestore
                        cargarDatosUsuario()
                    }
                    .addOnFailureListener { e ->
                        // Manejar errores al actualizar los datos en Firestore
                    }
            }
        }
    }

    fun actualizarBarraNivel(nuevoValor: Int) {
        // Obtener el valor actual de barraniv
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val docRef = FirebaseFirestore.getInstance().collection("infouser").document(uid)
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val valorActualString = snapshot.getString("barraniv") ?: "0" // Obtener el valor como cadena, usar "0" si es nulo
                val valorActual = valorActualString.toInt() // Convertir la cadena a un entero
                val nuevoValorTotal = valorActual + nuevoValor

                // Actualizar el valor de barraniv en Firestore
                transaction.update(docRef, "barraniv", nuevoValorTotal.toString()) // Actualizar Firestore con el valor convertido a cadena nuevamente
            }.addOnSuccessListener {
                // Éxito al actualizar la transacción
                cargarDatosUsuario()
            }.addOnFailureListener { e ->
                // Manejar errores
                Log.e("ERROR", "Error al actualizar barra de nivel: ${e.message}", e)
            }
        }
    }







//    fun actualizarNivel(nuevoValor: Int) {
//        actualizarDatoUsuario("niv", nuevoValor.toString())
//        actualizarDatoUsuario("barraniv", nuevoValor.toString())
//        cargarDatosUsuario()
//    }

    // En tu ViewModel
    fun actualizarSalud(nuevoValor: Int, nuevoValorBarra: Int) {
        actualizarDatoUsuario("salut", nuevoValor.toString())
        actualizarDatoUsuario("barrasalut", nuevoValorBarra.toString())
        cargarDatosUsuario()
    }

    fun actualizarComida(nuevoValor: Int, nuevoValorBarra: Int) {
        actualizarDatoUsuario("comida", nuevoValor.toString())
        actualizarDatoUsuario("barracomida", nuevoValorBarra.toString())
        cargarDatosUsuario()
    }

    fun actualizarBebida(nuevoValor: Int, nuevoValorBarra: Int) {
        actualizarDatoUsuario("bebida", nuevoValor.toString())
        actualizarDatoUsuario("barrabebida", nuevoValorBarra.toString())
        cargarDatosUsuario()
    }
    fun actualizarmenosSalud( nuevoValorBarra: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val docRef = FirebaseFirestore.getInstance().collection("infouser").document(uid)
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val valorActualBarraSalud = snapshot.getString("barrasalut")?.toIntOrNull() ?: 0
                val nuevoValorBarraSalud = maxOf(0, valorActualBarraSalud - nuevoValorBarra)

                // Actualizar el valor de salud y su barra correspondiente en Firestore

                transaction.update(docRef, "barrasalut", nuevoValorBarraSalud.toString())
            }.addOnSuccessListener {
                // Éxito al actualizar la transacción
                cargarDatosUsuario()
            }.addOnFailureListener { e ->
                // Manejar errores
                Log.e("ERROR", "Error al actualizar salud: ${e.message}", e)
            }
        }
    }

    fun actualizarmenosComida(nuevoValorBarra: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val docRef = FirebaseFirestore.getInstance().collection("infouser").document(uid)
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val valorActualBarraComida = snapshot.getString("barracomida")?.toIntOrNull() ?: 0
                val nuevoValorBarraComida = maxOf(0, valorActualBarraComida - nuevoValorBarra)

                // Actualizar el valor de comida y su barra correspondiente en Firestore

                transaction.update(docRef, "barracomida", nuevoValorBarraComida.toString())
            }.addOnSuccessListener {
                // Éxito al actualizar la transacción
                cargarDatosUsuario()
            }.addOnFailureListener { e ->
                // Manejar errores
                Log.e("ERROR", "Error al actualizar comida: ${e.message}", e)
            }
        }
    }

    fun actualizarmenosBebida(nuevoValorBarra: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val docRef = FirebaseFirestore.getInstance().collection("infouser").document(uid)
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val valorActualBarraBebida = snapshot.getString("barrabebida")?.toIntOrNull() ?: 0
                val nuevoValorBarraBebida = maxOf(0, valorActualBarraBebida - nuevoValorBarra)

                // Actualizar el valor de bebida y su barra correspondiente en Firestore
                transaction.update(docRef, "barrabebida", nuevoValorBarraBebida.toString())
            }.addOnSuccessListener {
                // Éxito al actualizar la transacción
                cargarDatosUsuario()
            }.addOnFailureListener { e ->
                // Manejar errores
                Log.e("ERROR", "Error al actualizar bebida: ${e.message}", e)
            }
        }
    }


    private fun actualizarDatoUsuario(nombreCampo: String, nuevoValor: String) {
        val datosUsuario = datosUsuario.value?.get(0)
        datosUsuario?.let {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val nuevoDatosUsuario = hashMapOf(
                    nombreCampo to nuevoValor // Actualizar el valor del campo especificado
                )

                firestore.collection("infouser").document(uid)
                    .update(nuevoDatosUsuario as Map<String, Any>)
                    .addOnSuccessListener {
                        // Éxito al actualizar los datos en Firestore
                    }
                    .addOnFailureListener { e ->
                        // Manejar errores al actualizar los datos en Firestore
                    }
            }
        }
    }





}