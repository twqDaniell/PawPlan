package com.example.pawplan.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pawplan.models.MainViewModel

@Composable
fun FoodScreen(
    foodImageUrl: String?,
    allergies: List<String>,
    mainViewModel: MainViewModel = viewModel()
) {
    val petDetails by mainViewModel.petDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Help Text Section
        FoodHeaderSection(petDetails?.petName ?: "Unknown")

        // Food Image Section
        FoodImageSection(foodImageUrl)

        // Allergies Section
        FoodAllergiesSection(allergies)
    }
}

