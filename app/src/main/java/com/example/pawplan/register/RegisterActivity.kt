package com.example.pawplan.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.pawplan.R

class RegisterActivity : AppCompatActivity() {
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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