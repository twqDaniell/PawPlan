package com.example.pawplan

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.pawplan.login.SignInActivity
import com.example.pawplan.models.MainViewModel
import com.example.pawplan.ui.theme.LatoFontFamily
import com.example.pawplan.ui.theme.PawPlanTheme
import com.google.firebase.auth.FirebaseAuth

val CustomTypography = Typography(
    bodyLarge = Typography().bodyLarge.copy(fontFamily = LatoFontFamily),
    titleLarge = Typography().titleLarge.copy(fontFamily = LatoFontFamily),
    labelSmall = Typography().labelSmall.copy(fontFamily = LatoFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = LatoFontFamily),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = LatoFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = LatoFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = LatoFontFamily),
)

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fetch user and pet details
        mainViewModel.fetchUserAndPetDetails()

        setContent {
            PawPlanTheme (
                typography = CustomTypography
            ) {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BarsWithScaffold({ handleLogout() })
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            MaterialTheme.colorScheme.primary.toArgb(),
                            MaterialTheme.colorScheme.primary.toArgb()
                        )
                    )
                }
            }
        }
    }

    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut() // Log out from Firebase

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Create an intent to navigate to SignInActivity
        val intent = Intent(this, SignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Start the new activity
        startActivity(intent)

        // Finish MainActivity
        finish()
    }

}