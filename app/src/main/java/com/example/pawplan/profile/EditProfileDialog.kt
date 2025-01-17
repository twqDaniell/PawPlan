package com.example.pawplan.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pawplan.models.PetDetails
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

@Composable
fun EditProfileDialog(
    petDetails: PetDetails,
    onSave: (PetDetails) -> Unit,
    onCancel: () -> Unit,
    fetchBreeds: (onBreedsFetched: (List<String>) -> Unit) -> Unit
) {
    var petName by remember { mutableStateOf(petDetails.petName) }
    var petBreed by remember { mutableStateOf(petDetails.petBreed) }
    var petWeight by remember { mutableStateOf(petDetails.petWeight.toString()) }
    var petColor by remember { mutableStateOf(petDetails.petColor) }
    var petPictureUri by remember { mutableStateOf<String?>(null) } // Selected picture URI
    var isUploading by remember { mutableStateOf(false) } // Upload progress state

    val storage = FirebaseStorage.getInstance()

    var breedOptions by remember { mutableStateOf(listOf<String>()) }
    var breedDropdownExpanded by remember { mutableStateOf(false) }
    var colorDropdownExpanded by remember { mutableStateOf(false) }
    val colorOptions = listOf("Brown", "White", "Grey", "Black", "Orange", "Multicolor")

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                petPictureUri = it.toString() // Store selected URI
            }
        }
    )

    // Fetch breeds when the dialog opens
    LaunchedEffect(Unit) {
        fetchBreeds { breeds ->
            breedOptions = breeds
        }
    }

    // Check if the save button should be enabled
    val isSaveEnabled = petName.isNotBlank() &&
            petBreed.isNotBlank() &&
            petWeight.isNotBlank() &&
            petColor.isNotBlank() &&
            (petName != petDetails.petName ||
                    petBreed != petDetails.petBreed ||
                    petWeight != petDetails.petWeight.toString() ||
                    petColor != petDetails.petColor ||
                    petPictureUri != null) // Check if picture was selected

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text("Edit Pet Profile") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Picture section
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    // Display current picture or selected one
                    val painter = rememberAsyncImagePainter(
                        model = petPictureUri ?: petDetails.picture
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Pet Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") } // Open image picker
                    )
                    // Edit Icon Overlay
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .clickable { launcher.launch("image/*") }
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Picture",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isUploading = true
                    if (petPictureUri != null) {
                        val fileName = "${petDetails.petId}_${System.currentTimeMillis()}.jpg"
                        val storageRef = storage.reference.child("pet_pictures/$fileName")
                        val uploadTask = storageRef.putFile(android.net.Uri.parse(petPictureUri))
                        uploadTask.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val updatedPetDetails = petDetails.copy(
                                    petName = petName,
                                    petBreed = petBreed,
                                    petWeight = petWeight.toIntOrNull() ?: petDetails.petWeight,
                                    petColor = petColor,
                                    picture = downloadUri.toString()
                                )
                                isUploading = false
                                onSave(updatedPetDetails)
                            }
                        }.addOnFailureListener {
                            isUploading = false
                            println("Failed to upload picture: ${it.message}")
                        }
                    } else {
                        val updatedPetDetails = petDetails.copy(
                            petName = petName,
                            petBreed = petBreed,
                            petWeight = petWeight.toIntOrNull() ?: petDetails.petWeight,
                            petColor = petColor,
                            picture = petDetails.picture // Keep existing picture if none is selected
                        )
                        isUploading = false
                        onSave(updatedPetDetails)
                    }
                },
                enabled = isSaveEnabled && !isUploading // Disable button while uploading
            ) {
                Text(if (isUploading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            Button(onClick = { onCancel() }) {
                Text("Cancel")
            }
        }
    )
}
