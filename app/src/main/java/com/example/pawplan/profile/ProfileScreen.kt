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
import com.example.pawplan.models.PetDetails

@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    val petDetails by mainViewModel.petDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PetDetailsSection(mainViewModel)
        if (petDetails != null) {
            ProfileActionsSection(
                currentPetDetails = petDetails!!,
                onUpdatePetDetails = { updatedDetails ->
                    mainViewModel.updatePetDetails(updatedDetails)
                },
                fetchBreeds = { onBreedsFetched ->
                    mainViewModel.fetchBreeds(onBreedsFetched)
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        MemoriesSection(petDetails?.petId ?: "UnknownPetId", petDetails?.petName ?: "Unknown name")
    }
}

