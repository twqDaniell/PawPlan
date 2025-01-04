package com.example.pawplan.health

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawplan.models.MainViewModel

@Composable
fun HealthScreen(mainViewModel: MainViewModel = viewModel()) {
    val petDetails by mainViewModel.petDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        HeaderSection(petDetails?.petName ?: "Unknown name")

        VetInfoSection()

        VaccinationRecordsSection()

        FoodAllergiesSection()
    }
}
