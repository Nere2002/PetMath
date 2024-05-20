package com.skye.petmath.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.skye.petmath.MainActivity
import com.skye.petmath.R
import com.skye.petmath.SharedViewModel
import com.skye.petmath.databinding.FragmentInicioBinding
import com.skye.petmath.databinding.FragmentMatesBinding
import com.skye.petmath.tienda.TiendaVewModel
import kotlin.math.pow
import kotlin.math.round
import kotlin.random.Random
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth



class MatesFragment : Fragment() {
    private lateinit var binding: FragmentMatesBinding
    private lateinit var mathProblems: List<String>
    private var currentProblemIndex = 0
    private var correctAnswers = 0
    private var numberOfDigits = 1 // Valor predeterminado
    private val userAnswersMap = mutableMapOf<Int, String>()
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatesBinding.inflate(inflater, container, false)
        val view = binding.root
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)


        val operations = arrayOf("Sumar", "Restar", "Multiplicar", "Dividir")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, operations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOperations.adapter = adapter

        binding.seekBarDigits.max = 4 // Máximo de 4 dígitos
        binding.seekBarDigits.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                numberOfDigits = progress + 1 // El progreso va de 0 a 3, pero queremos de 1 a 4
                binding.tvDigits.text = numberOfDigits.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

//        binding.btnStart.setOnClickListener {
//            val operation = operations[binding.spinnerOperations.selectedItemPosition]
//            mathProblems = generateMathProblems(operation, numberOfDigits)
//            currentProblemIndex = 0
//            showCurrentProblem()
//            clearResults() // Limpia los resultados al iniciar una nueva serie de problemas
//        }
        binding.btnStart.setOnClickListener {
            val docRef = FirebaseFirestore.getInstance().collection("infouser").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            docRef.get()
                .addOnSuccessListener { document ->
                    val barracomida = document.getString("barracomida") ?: "0"
                    val barrabebida = document.getString("barrabebida") ?: "0"
                    val barrasalud = document.getString("barrasalut") ?: "0"

                    if (barracomida == "0" || barrabebida == "0" || barrasalud == "0") {
                        // Si alguna de las barras está en 0, mostrar un mensaje al usuario
                        val message = when {
                            barracomida == "0" -> "Sube la comida."
                            barrabebida == "0" -> "Sube la bebida."
                            barrasalud == "0" -> "Sube la salud."
                            else -> "Algo salió mal."
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    } else {
                        // Si todas las barras son diferentes de 0, permitir al usuario jugar
                        val operation = operations[binding.spinnerOperations.selectedItemPosition]
                        mathProblems = generateMathProblems(operation, numberOfDigits)
                        currentProblemIndex = 0
                        showCurrentProblem()
                        clearResults() // Limpia los resultados al iniciar una nueva serie de problemas
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ERROR", "Error al obtener datos del usuario: ", exception)
                }
        }



        binding.btnSubmitAnswer.setOnClickListener {
            val userAnswer = binding.etUserAnswer.text.toString()
            userAnswersMap[currentProblemIndex] = userAnswer // Almacena la respuesta del usuario
            val correctAnswer = getCorrectAnswer(mathProblems[currentProblemIndex])
            if (userAnswer == correctAnswer) {
                binding.tvResult.text = "Respuesta Correcta"
                binding.tvResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                correctAnswers++
            } else {
                binding.tvResult.text = "Respuesta Incorrecta"
                binding.tvResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
            currentProblemIndex++
            if (currentProblemIndex < mathProblems.size) {
                showCurrentProblem()
            } else {
                showResults()
            }
        }


        return view
    }
// ------------------------ METODOS -------------------------------
    private fun generateMathProblems(operation: String, numberOfDigits: Int): List<String> {
        val problems = mutableListOf<String>()
        repeat(10) {
            val operand1 = generateRandomNumber(numberOfDigits)
            val operand2 = if (operation == "Restar") {
                generateRandomNumberLessThanOrEqualTo(operand1, numberOfDigits)
            } else {
                generateRandomNumber(numberOfDigits)
            }
            val problem = when (operation) {
                "Sumar" -> "$operand1 + $operand2 = ${operand1 + operand2}"
                "Restar" -> "$operand1 - $operand2 = ${operand1 - operand2}"
                "Multiplicar" -> "$operand1 × $operand2 = ${operand1 * operand2}"
                "Dividir" -> {
                    // Evitar divisiones con decimales, reintentar si el resultado no es entero
                    val result = operand1 / operand2
                    "$operand1 ÷ $operand2 = ${result.toInt()}"
                }
                else -> ""
            }
            problems.add(problem)
        }
        return problems
    }





    private fun generateRandomNumberLessThanOrEqualTo(max: Int, numberOfDigits: Int): Int {
        val min = 10.0.pow(numberOfDigits.toDouble() - 1).toInt()
        return Random.nextInt(min, max + 1)
    }

    private fun generateRandomNumber(numberOfDigits: Int): Int {
        val min = 10.0.pow(numberOfDigits.toDouble() - 1).toInt()
        val max = 10.0.pow(numberOfDigits.toDouble()).toInt() - 1
        return Random.nextInt(min, max)
    }

    private fun showCurrentProblem() {
        if (currentProblemIndex < mathProblems.size) {
            val problem = mathProblems[currentProblemIndex]
            val operationWithoutResult = problem.split(" = ")[0] // Obtiene la operación sin el resultado
            binding.tvProblem.text = operationWithoutResult
            binding.tvProblem.visibility = View.VISIBLE
            binding.tvResult.visibility = View.GONE // Ocultar el TextView de resultado
            binding.etUserAnswer.text.clear()
            binding.etUserAnswer.visibility = View.VISIBLE
            binding.btnSubmitAnswer.visibility = View.VISIBLE

            // Limpiar resultados anteriores del usuario
            clearResults()
        } else {
            // Si no hay más problemas que mostrar, ocultar elementos de entrada y mostrar resultados
            binding.tvProblem.visibility = View.GONE
            binding.etUserAnswer.visibility = View.GONE
            binding.btnSubmitAnswer.visibility = View.GONE
            showResults()
        }
    }

    private fun showResults() {
        val totalProblems = mathProblems.size
        val message = "Respuestas correctas: $correctAnswers / $totalProblems\nRespuestas incorrectas: ${totalProblems - correctAnswers}"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        sharedViewModel.actualizarNumeroMonedas(correctAnswers)
        sharedViewModel.actualizarBarraNivel(correctAnswers)
        sharedViewModel.actualizarmenosBebida(correctAnswers)
        sharedViewModel.actualizarmenosComida(correctAnswers)
        sharedViewModel.actualizarmenosSalud(correctAnswers)


        // Mostrar resultados de cada problema
        val problemResults = StringBuilder()
        mathProblems.forEachIndexed { index, problem ->
            val correctAnswer = getCorrectAnswer(problem)
            val userAnswer = userAnswersMap[index]
            val operationWithoutResult = problem.split(" = ")[0] // Obtener la operación sin el resultado
            val result = if (userAnswer != null) {
                val userResult = if (userAnswer == correctAnswer) "Correcta" else "Incorrecta"
                "$operationWithoutResult -> $userAnswer ($userResult) -> $correctAnswer"
            } else {
                "$operationWithoutResult -> No disponible"
            }
            problemResults.append("$result\n")
        }
        binding.tvProblemResult.text = problemResults.toString()
        binding.tvProblemResult.visibility = View.VISIBLE

        // Reiniciar el conteo de respuestas correctas
        correctAnswers = 0
    }

    private fun getCorrectAnswer(problem: String): String {
        val parts = problem.split(" ", " = ")
        val operand1 = parts[0].toInt()
        val operand2 = parts[2].toInt()
        val operator = parts[1]
        val result = when (operator) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "×" -> operand1 * operand2
            "÷" -> operand1 / operand2 // Siempre se generan problemas con resultados enteros
            else -> 0
        }
        return result.toString()
    }




    private fun clearResults() {
        binding.tvResult.text = "" // Limpiar el TextView de resultado
        binding.tvProblemResult.text = "" // Limpiar el TextView de resultados
        // No se borra el mapa de respuestas del usuario
    }

    private fun truncateDecimal(number: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces.toDouble())
        return round(number * factor) / factor
    }

}