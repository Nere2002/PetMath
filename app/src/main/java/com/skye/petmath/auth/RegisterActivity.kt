package com.skye.petmath.auth

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.petmath.MainActivity
import com.skye.petmath.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener {
            registerUser()

        }
    }
    private fun registerUser() {
        val username =  binding.etUsername.text.toString()
        val password =  binding.etPassword.text.toString()

        // Check if the email is already in use
        auth.fetchSignInMethodsForEmail(username).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null && result.signInMethods != null && result.signInMethods!!.isNotEmpty()) {
                    // Email is already in use
                    Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show()
                } else {
                    // Email is not in use, proceed with registration
                    auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userInfo = hashMapOf(
                                "email" to username,
                                "password" to password
                            )
                            if (user != null) {
                                // Save user info to Firestore
                                db.collection("user").document(user.uid)
                                    .set(userInfo)
                                    .addOnSuccessListener {
                                        // After successfully saving user info, create a document in "info_user"
                                        val infoData = hashMapOf(
                                            "uid" to user.uid,
                                            "barrabebida" to "0",
                                            "barracomida" to "0",
                                            "barrasalut" to "0",
                                            "barraniv" to "0",
                                            "bebida" to "0",
                                            "comida" to "0",
                                            "moneda" to "10",
                                            "niv" to "0",
                                            "salut" to "0"
                                        )

                                        Toast.makeText(
                                            this,
                                            "User creado correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        db.collection("infouser").document(user.uid)
                                            .set(infoData)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "infouser creeado correctamente", Toast.LENGTH_SHORT).show()

                                                // Redirect to MainActivity after successful registration
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish() // Close this activity
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Error al crear el infouser", e)
                                                Toast.makeText(this, "Error al crear el infouser", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error al añadir user document", e)
                                        Toast.makeText(
                                            this,
                                            "Error al añadir el documento",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task.exception)
                            Toast.makeText(
                                this,
                                "Registro fallido: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Log.w(TAG, "fetchSignInMethodsForEmail:failure", task.exception)
                Toast.makeText(
                    this,
                    "El email ya existe : ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}