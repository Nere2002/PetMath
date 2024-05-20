package com.skye.petmath.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel

import com.skye.petmath.R
import com.skye.petmath.SharedViewModel
import com.skye.petmath.databinding.FragmentInicioBinding


class InicioFragment : Fragment() {
    // Declaración de variables
    private lateinit var binding: FragmentInicioBinding
    private val handler = Handler(Looper.getMainLooper())
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento y vincular vistas
        binding = FragmentInicioBinding.inflate(inflater, container, false)

        // Vinculación de vistas
        val numnivel = binding.numnivel
        val barranivel = binding.barranivel
        val numsalut = binding.numsalut
        val barrasalut = binding.barrasalut
        val numcomida = binding.numcomida
        val barracomida = binding.barracomida
        val numbebida = binding.numbebida
        val barrabebida = binding.barrabebida
        val imagengato = binding.imageView3

        // Observar cambios en los datos del usuario
        viewModel.datosUsuario.observe(viewLifecycleOwner, Observer { listaDatosUsuario ->
            if (listaDatosUsuario.isNotEmpty()) {
                val datosUsuario = listaDatosUsuario[0]

                // Nivel
                val numeroNivel = datosUsuario.niv?.toIntOrNull()
                if (numeroNivel != null) {
                    numnivel.text = numeroNivel.toString()
                    val numeroBarraNivel = datosUsuario.barraniv?.toInt() ?: 0
                    barranivel.progress = numeroBarraNivel

                    // Cambiar la imagen del gato según el nivel
                    when (numeroNivel) {
                        in 0..25 -> imagengato.setImageResource(R.mipmap.gato_foreground)
                        in 26..59 -> imagengato.setImageResource(R.mipmap.gatoevo_foreground)
                        in 60..100 -> imagengato.setImageResource(R.mipmap.gato33_foreground)
                        else -> imagengato.setImageResource(R.mipmap.gato_foreground) // Imagen predeterminada
                    }

                    // Incrementar nivel cuando numerobarranivel llega a 100
                    if (numeroBarraNivel == 100) {
                        val nuevoNivel = numeroNivel + 1
                        viewModel.actualizarNivel(nuevoNivel, 0)
                    }
                }

                // Salud
                val numeroSalud = datosUsuario.salut?.toIntOrNull()
                if (numeroSalud != null) {
                    numsalut.text = numeroSalud.toString()
                    val numerobarrasalut = datosUsuario.barrasalut?.toInt() ?: 0
                    barrasalut.progress = numerobarrasalut
                }

                // Comida
                val numeroComida = datosUsuario.comida?.toIntOrNull()
                if (numeroComida != null) {
                    numcomida.text = numeroComida.toString()
                    val numerobarracomida = datosUsuario.barracomida?.toInt() ?: 0
                    barracomida.progress = numerobarracomida
                }

                // Bebida
                val numeroBebida = datosUsuario.bebida?.toIntOrNull()
                if (numeroBebida != null) {
                    numbebida.text = numeroBebida.toString()
                    val numerobarrabebida = datosUsuario.barrabebida?.toInt() ?: 0
                    barrabebida.progress = numerobarrabebida
                }
            }
        })

        // Configurar el temporizador para decrementar la salud
        handler.postDelayed(updateSalutRunnable, 10 * 60 * 1000)

        // Configurar onClickListener para el botón de salud
        binding.botonsalut.setOnClickListener {
            handleBotonAccion(binding.numsalut, binding.barrasalut)
        }

        // Configurar onClickListener para el botón de comida
        binding.botoncomida.setOnClickListener {
            handleBotonAccion(binding.numcomida, binding.barracomida)
        }

        // Configurar onClickListener para el botón de bebida
        binding.botonbebida.setOnClickListener {
            handleBotonAccion(binding.numbebida, binding.barrabebida)
        }

        return binding.root
    }

    // Función para manejar los botones de acción (salud, comida, bebida)
    private fun handleBotonAccion(numTextView: TextView, barra: ProgressBar) {
        if (numTextView.text.toString().toInt() == 0) {
            Toast.makeText(context, "¡Compra más!", Toast.LENGTH_SHORT).show()
        } else {
            if (barra.progress < 100) {
                barra.progress += 10
                numTextView.text = (numTextView.text.toString().toInt() - 1).toString()

                // Actualizar en la base de datos
                when (numTextView) {
                    binding.numsalut -> viewModel.actualizarSalud(numTextView.text.toString().toInt(), barra.progress)
                    binding.numcomida -> viewModel.actualizarComida(numTextView.text.toString().toInt(), barra.progress)
                    binding.numbebida -> viewModel.actualizarBebida(numTextView.text.toString().toInt(), barra.progress)
                }
            } else {
                Toast.makeText(context, "¡Máximo alcanzado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Runnable para decrementar la salud con el tiempo
    private val updateSalutRunnable = object : Runnable {
        override fun run() {
            decrementarSalud()
            handler.postDelayed(this, 10 * 60 * 1000)
        }
    }

    // Función para decrementar la salud
    private fun decrementarSalud() {
        val numsalutTextView = binding.numsalut
        val barrasalut = binding.barrasalut

        if (barrasalut.progress > 0 && numsalutTextView.text.toString().toInt() > 0) {
            barrasalut.progress -= 10
            numsalutTextView.text = (numsalutTextView.text.toString().toInt() - 1).toString()
        } else {
            handler.removeCallbacks(updateSalutRunnable)
            Toast.makeText(context, "¡La salud está al máximo!", Toast.LENGTH_SHORT).show()
            if (barrasalut.progress == 100) {
                // Incrementar la barra de nivel en 2 si la barra de salud llega a 100
                binding.barranivel.progress += 2
                binding.barrasalut.progress = 90
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSalutRunnable)
    }


}