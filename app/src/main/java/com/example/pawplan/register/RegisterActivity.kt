package com.example.pawplan.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.pawplan.R
import com.example.pawplan.models.RegistrationViewModel

class RegisterActivity : AppCompatActivity() {
    var navController: NavController? = null
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        val phoneNumber = intent.getStringExtra("phoneNumber")
        viewModel.phoneNumber = phoneNumber

        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_register) as? NavHostFragment
        navController = navHostFragment?.navController

        navController?.let { navController ->
            // Navigate to the first fragment (optional)
            if (savedInstanceState == null) { // Avoid navigation on recreation
                navController.navigate(R.id.registerName)
            }
        }
    }
}