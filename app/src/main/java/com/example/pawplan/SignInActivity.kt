package com.example.pawplan

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI

class SignInActivity : AppCompatActivity() {
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController

        Log.d("SignInActivity", "Navigation Graph Loaded: ${navController}")

        navController?.let { navController ->
            // Navigate to the first fragment (optional)
            if (savedInstanceState == null) { // Avoid navigation on recreation
                navController.navigate(R.id.signInFormFragment)
            }
        }
    }
}
