package com.example.pawplan

import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
                MissingScreen() // Your Missing Screen Composable
            }
            composable("food") {
                FoodScreen() // Your Food Screen Composable
            }
        }
    }
}
