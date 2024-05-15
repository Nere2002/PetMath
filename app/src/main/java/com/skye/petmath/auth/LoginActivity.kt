package com.skye.petmath.auth

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.skye.petmath.MainActivity
import com.skye.petmath.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
class loginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuario ya autenticado, redirigir a la actividad principal
            startActivity(Intent(this, MainActivity::class.java)
                .putExtra("user_email", currentUser.email))
            finish()  // Cerrar esta actividad para evitar que el usuario retroceda a la pantalla de inicio de sesión
        } else {
            // Si el usuario no está autenticado, inicia sesión con el banner por defecto
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putInt("user_banner", 0).apply()

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        val btnLogin = binding.btnLogin
        val btnRegister = binding.btnRegister
        val progressBar = binding.progressBar
        val tvErrorMessage = binding.tvErrorMessage
        btnLogin.setOnClickListener {
            loginUser()
        }
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loginUser(){
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        if (username.isNotEmpty() && password.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvErrorMessage.visibility = View.GONE

            auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Login successful, redirect to MainActivity
                        val user = auth.currentUser
                        if (user != null) {
                            // Retrieve user data from Firestore
                            db.collection("user").document(user.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        // User data existe, redirigir a MainActivity
                                        // El inicio de sesión fue exitoso
                                        Toast.makeText(this, "Inicio de sesión exitoso. ¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                        // Guardar el correo para despues mostrarlo en el menu lateral
                                        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                        sharedPrefs.edit().putString("username", username).apply()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()  // Close this activity
                                    } else {
                                        // User data doesn't exist, redirect to setup screen or handle accordingly
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error getting user data", exception)
                                    Toast.makeText(this, "Error getting user data", Toast.LENGTH_SHORT).show()
                                }
                        }


                    } else {
                        Toast.makeText(this, "ERROR al Iniciar Sesion", Toast.LENGTH_SHORT).show()
                        // Si el inicio de sesión falla, mostrar un mensaje de error
                        binding.tvErrorMessage.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = "Error al iniciar sesión. Verifica tus credenciales."
                    }
                }
        } else {
            binding.tvErrorMessage.visibility = View.VISIBLE
            binding.tvErrorMessage.text = "Por favor, completa todos los campos."
        }
    }
}
