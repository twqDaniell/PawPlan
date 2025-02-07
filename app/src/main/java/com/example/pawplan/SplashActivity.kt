package com.example.pawplan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pawplan.login.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is logged in, go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // User is not logged in, go to LoginActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        // Close SplashActivity
        finish()
    }
}
