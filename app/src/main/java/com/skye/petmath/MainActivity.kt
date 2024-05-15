package com.skye.petmath

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.petmath.auth.loginActivity
import com.skye.petmath.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    // Variables
    private lateinit var binding: ActivityMainBinding
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Firebase
        db = FirebaseFirestore.getInstance()

        // Configuración de la barra de herramientas
        setSupportActionBar(binding.appBarMain.toolbar)

        // Configuración de la navegación
        setupNavigation()

        // Mostrar el correo del usuario
        displayUserEmail()

        // Observar cambios en el número de monedas
        observeCoins()
    }

    // Configurar la navegación
    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.findNavController()

        val drawerLayout = binding.drawerLayout

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.inicioFragment,
                R.id.matesFragment,
                R.id.tiendaFragment
            ), drawerLayout
        )
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        binding.navView.setupWithNavController(navController)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loginActivity -> {
                    menuItem.isChecked = false
                    signOut()
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    // Mostrar el correo del usuario
    private fun displayUserEmail() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("user").document(currentUser.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val email = documentSnapshot.getString("email")
                        if (!email.isNullOrEmpty()) {
                            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.mostrarcorreo).text = email
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejar errores al obtener el correo electrónico del usuario
                }
        }
    }

    // Observar cambios en el número de monedas
    private fun observeCoins() {
        val textViewMonedas: TextView = binding.appBarMain.nummonedas

        viewModel.cargarDatosUsuario() // Asegúrate de llamar a cargarDatosUsuario aquí

        viewModel.datosUsuario.observe(this, Observer { listaDatosUsuario ->
            if (listaDatosUsuario.isNotEmpty()) {
                val datosUsuario = listaDatosUsuario[0]
                val numeroMonedas = datosUsuario.moneda?.toInt() ?: 0
                textViewMonedas.text = numeroMonedas.toString()
            }
        })
    }



    // Método para cerrar sesión
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, loginActivity::class.java)
        startActivity(intent)
        finish()
    }

}