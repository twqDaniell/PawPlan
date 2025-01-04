package com.example.pawplan.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawplan.models.PetDetails

@Composable
fun ProfileActionsSection(
    currentPetDetails: PetDetails,
    onUpdatePetDetails: (PetDetails) -> Unit,
    fetchBreeds: (onBreedsFetched: (List<String>) -> Unit) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = { showEditDialog = true }) {
            Text("Edit Profile")
        }
        Button(onClick = { /* Lost Pet Logic */ }) {
            Text("Lost My Pet")
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            petDetails = currentPetDetails,
            onSave = { updatedDetails ->
                onUpdatePetDetails(updatedDetails)
                showEditDialog = false
            },
            onCancel = { showEditDialog = false },
            fetchBreeds = fetchBreeds
        )
    }
}

