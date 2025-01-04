package com.example.pawplan.foods

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Allergy(
    val petId: String = "",
    val allergyName: String = "",
)

@Composable
fun FoodAllergiesSection(
    petName: String,
    petId: String
) {
    val allergies = remember { mutableStateOf<List<Allergy>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var newAllergyName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Fetch allergies when the petId changes
    LaunchedEffect(petId) {
        try {
            val petAllergies = fetchAllergiesForPet(petId) // Call the suspend function
            allergies.value = petAllergies
        } catch (e: Exception) {
            Log.e("FoodAllergiesSection", "Error fetching allergies: ${e.message}")
        }
    }

    // Food Allergies Section
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${petName}’s Food Allergies",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Allergy",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // List of Allergies
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            allergies.value.forEach { allergy ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Close, // Allergy icon
                        contentDescription = "Allergy",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = allergy.allergyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Popup for Adding Allergy
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Add Allergy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newAllergyName,
                        onValueChange = { newAllergyName = it },
                        label = { Text("Allergy Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Launch a coroutine for saving the allergy
                        coroutineScope.launch {
                            try {
                                saveAllergyToFirestore(
                                    Allergy(petId = petId, allergyName = newAllergyName)
                                )
                                showDialog = false
                                newAllergyName = ""

                                // Reload allergies after adding
                                val updatedAllergies = fetchAllergiesForPet(petId)
                                allergies.value = updatedAllergies
                            } catch (e: Exception) {
                                Log.e("FoodAllergiesSection", "Error saving allergy: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


suspend fun fetchAllergiesForPet(petId: String): List<Allergy> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("allergies")
            .whereEqualTo("petId", petId)
            .get()
            .await() // Wait for the query to complete

        // Map the documents to a list of Allergy objects
        snapshot.documents.mapNotNull { document ->
            document.toObject(Allergy::class.java)
        }
    } catch (e: Exception) {
        Log.e("FoodAllergiesSection", "Error fetching allergies: ${e.message}")
        emptyList()
    }
}

suspend fun saveAllergyToFirestore(allergy: Allergy) {
    val firestore = FirebaseFirestore.getInstance()
    try {
        firestore.collection("allergies")
            .add(allergy) // Save the allergy
            .await()
        Log.d("FoodAllergiesSection", "Allergy saved successfully")
    } catch (e: Exception) {
        Log.e("FoodAllergiesSection", "Error saving allergy: ${e.message}")
    }
}
