package com.example.pawplan

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pawplan.foods.FoodScreen
import com.example.pawplan.health.HealthScreen
import com.example.pawplan.login.SignInActivity
import com.example.pawplan.missing.MissingScreen
import com.example.pawplan.models.MainViewModel
import com.example.pawplan.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BarsWithScaffold(onLogoutClick: () -> Unit) {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    Scaffold(
        topBar = { SmallTopBar(
            onLogoutClick = {
                onLogoutClick()
            }
        ) }, // Reuse TopAppBar from another file
        bottomBar = { NavigationBarPawPlan(navController) } // Reuse NavigationBar from another file
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "profile", // Define the initial route
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("profile") {
                ProfileScreen(mainViewModel) // Your Profile Screen Composable
            }
            composable("health") {
                HealthScreen(mainViewModel) // Your Health Screen Composable
            }
            composable("missing") {
                MissingScreen(mainViewModel) // Your Missing Screen Composable
            }
            composable("food") {
                FoodScreen(
                    mainViewModel,
                    allergies = listOf("Artificial Additives", "Beef", "Soy"),
                ) // Your Food Screen Composable
            }
        }
    }
}