package com.example.pawplan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pawplan.health.HealthScreen
import com.example.pawplan.missing.MissingPetsScreenPreview
import com.example.pawplan.missing.MissingScreen
import com.example.pawplan.profile.ProfileScreen

@Composable
fun BarsWithScaffold() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { SmallTopBar() }, // Reuse TopAppBar from another file
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
                ProfileScreen() // Your Profile Screen Composable
            }
            composable("health") {
                HealthScreen() // Your Health Screen Composable
            }
            composable("missing") {
                MissingPetsScreenPreview() // Your Missing Screen Composable
            }
            composable("food") {
                FoodScreen() // Your Food Screen Composable
            }
        }
    }
}
