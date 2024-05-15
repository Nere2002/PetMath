package com.skye.petmath.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.petmath.DatosUsuario
import com.skye.petmath.SharedViewModel
import com.skye.petmath.databinding.FragmentTiendaBinding
import com.skye.petmath.tienda.TiendaAdapter
import com.skye.petmath.tienda.TiendaVewModel
import com.skye.petmath.tienda.tiendaitems
import kotlin.math.log


class TiendaFragment : Fragment() {

    private lateinit var binding: FragmentTiendaBinding
    private lateinit var viewModel: TiendaVewModel
    private lateinit var SharedviewModel: SharedViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var datosUsuario: DatosUsuario

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTiendaBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(TiendaVewModel::class.java)
        SharedviewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java) // Inicializar SharedViewModel

        firestore = FirebaseFirestore.getInstance() // Inicializar firestore aquí

        setupRecyclerView(binding.recyclerComida, viewModel.comidaList)
        setupRecyclerView(binding.recyclerBebida, viewModel.bebidaList)
        setupRecyclerView(binding.recyclerJugete, viewModel.jugeteList)

        return binding.root
    }



    private fun setupRecyclerView(recyclerView: RecyclerView, dataList: List<tiendaitems>) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TiendaAdapter(dataList, this@TiendaFragment, requireContext())
    }


    fun comprarProducto(tienda: tiendaitems, costo: Int, tipoProducto: String) {
        Log.d("TiendaFragment", "comprarProducto: Ejecutado", )
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firestore.collection("infouser").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        datosUsuario = document.toObject(DatosUsuario::class.java)!!
                        val numeroMonedas = datosUsuario.moneda?.toInt() ?: 0
                        val incremento: Int = when {
                            costo in 0..10 -> 1
                            costo in 11..20 -> 2
                            else -> 3
                        }
                        if (numeroMonedas >= costo) {
                            // El usuario tiene suficientes monedas para comprar el producto
                            val nuevaCantidadMonedas = numeroMonedas - costo + costo
                            val nuevoDatosUsuario = hashMapOf(
                                "moneda" to nuevaCantidadMonedas.toString() // Actualizar el número de monedas
                            )

                            // Actualizar el campo correspondiente en DatosUsuario según el tipo de producto
                            when (tipoProducto) {
                                "comida" -> nuevoDatosUsuario["comida"] =
                                    ((datosUsuario.comida?.toInt() ?: 0) + incremento).toString()

                                "bebida" -> nuevoDatosUsuario["bebida"] =
                                    ((datosUsuario.bebida?.toInt() ?: 0) + incremento).toString()

                                "salut" -> nuevoDatosUsuario["salut"] =
                                    ((datosUsuario.salut?.toInt() ?: 0) + incremento).toString()
                            }

                            firestore.collection("infouser").document(uid)
                                .update(nuevoDatosUsuario as Map<String, Any>)
                                .addOnSuccessListener {
                                    // Éxito al actualizar los datos en Firestore
                                    SharedviewModel.actualizarNumeroMonedas1(costo) // Actualizar en el ViewModel
                                    // Mostrar un mensaje de éxito o realizar otras acciones
                                    Toast.makeText(
                                        requireContext(),
                                        "Compra realizada con éxito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    // Manejar errores al actualizar los datos en Firestore
                                    Toast.makeText(
                                        requireContext(),
                                        "Error al realizar la compra",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            // El usuario no tiene suficientes monedas para comprar el producto
                            // Mostrar un mensaje de advertencia según el tipo de producto
                            val mensaje = when (tipoProducto) {
                                "comida" -> "No tienes suficientes monedas para comprar esta comida."
                                "bebida" -> "No tienes suficientes monedas para comprar esta bebida."
                                "salut" -> "No tienes suficientes monedas para comprar este producto."
                                else -> "No tienes suficientes monedas para comprar este producto."
                            }
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar errores de consulta
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener datos del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }



}