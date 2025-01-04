package com.example.pawplan.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.pawplan.R
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    var navController: NavController? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController

        navController?.let { navController ->
            // Navigate to the first fragment (optional)
            if (savedInstanceState == null) { // Avoid navigation on recreation
                navController.navigate(R.id.signInFormFragment)
            }
        }
    }
}
