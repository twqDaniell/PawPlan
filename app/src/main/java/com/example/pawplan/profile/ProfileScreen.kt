package com.example.pawplan.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawplan.models.MainViewModel
import androidx.compose.runtime.getValue

@Composable
fun ProfileScreen(mainViewModel: MainViewModel = viewModel()) {
    val petDetails by mainViewModel.petDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PetDetailsSection(mainViewModel)
        ProfileActionsSection()
        MemoriesSection(petDetails?.petId ?: "UnknownPetId")
    }
}
