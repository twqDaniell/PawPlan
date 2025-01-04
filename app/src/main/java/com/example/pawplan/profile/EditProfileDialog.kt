package com.example.pawplan.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawplan.models.PetDetails
import java.util.Date

@Composable
fun EditProfileDialog(
    petDetails: PetDetails,
    onSave: (PetDetails) -> Unit,
    onCancel: () -> Unit,
    fetchBreeds: (onBreedsFetched: (List<String>) -> Unit) -> Unit // Function to fetch breeds
) {
    var petName by remember { mutableStateOf(petDetails.petName) }
    var petBreed by remember { mutableStateOf(petDetails.petBreed) }
    var petWeight by remember { mutableStateOf(petDetails.petWeight.toString()) }
    var petColor by remember { mutableStateOf(petDetails.petColor) }
    var petBirthDate by remember { mutableStateOf(petDetails.petBirthDate) }
    var petAdoptionDate by remember { mutableStateOf(petDetails.petAdoptionDate) }

    var breedOptions by remember { mutableStateOf(listOf<String>()) }
    var breedDropdownExpanded by remember { mutableStateOf(false) }
    var colorDropdownExpanded by remember { mutableStateOf(false) }
    val colorOptions = listOf("Brown", "White", "Grey", "Black", "Orange", "Multicolor")

    // Fetch breeds when the dialog opens
    LaunchedEffect(Unit) {
        fetchBreeds { breeds ->
            breedOptions = breeds
        }
    }

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text("Edit Pet Profile") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Breed Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = petBreed,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pet Breed") },
                        trailingIcon = {
                            IconButton(onClick = { breedDropdownExpanded = !breedDropdownExpanded }) {
                                Icon(
                                    imageVector = if (breedDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { breedDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = breedDropdownExpanded,
                        onDismissRequest = { breedDropdownExpanded = false }
                    ) {
                        breedOptions.forEach { breed ->
                            DropdownMenuItem(
                                onClick = {
                                    petBreed = breed
                                    breedDropdownExpanded = false
                                },
                                text = { Text(breed) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = petWeight,
                    onValueChange = { petWeight = it },
                    label = { Text("Pet Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = petColor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pet Color") },
                        trailingIcon = {
                            IconButton(onClick = { colorDropdownExpanded = !colorDropdownExpanded }) {
                                Icon(
                                    imageVector = if (colorDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { colorDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = colorDropdownExpanded,
                        onDismissRequest = { colorDropdownExpanded = false }
                    ) {
                        colorOptions.forEach { color ->
                            DropdownMenuItem(
                                onClick = {
                                    petColor = color
                                    colorDropdownExpanded = false
                                },
                                text = { Text(color) }
                            )
                        }
                    }
                }

//                OutlinedTextField(
//                    value = petBirthDate.toString(), // Format appropriately in real use
//                    onValueChange = { /* Date parsing logic here */ },
//                    label = { Text("Pet Birth Date") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = false // For simplicity; replace with date picker
//                )
//                OutlinedTextField(
//                    value = petAdoptionDate.toString(), // Format appropriately in real use
//                    onValueChange = { /* Date parsing logic here */ },
//                    label = { Text("Pet Adoption Date") },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = false // For simplicity; replace with date picker
//                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedPetDetails = petDetails.copy(
                    petName = petName,
                    petBreed = petBreed,
                    petWeight = petWeight.toIntOrNull() ?: petDetails.petWeight,
                    petColor = petColor,
                    petBirthDate = petBirthDate,
                    petAdoptionDate = petAdoptionDate
                )
                onSave(updatedPetDetails)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onCancel() }) {
                Text("Cancel")
            }
        }
    )
}

