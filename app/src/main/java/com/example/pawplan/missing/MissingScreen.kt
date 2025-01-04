package com.example.pawplan.missing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawplan.models.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun MissingScreen(mainViewModel: MainViewModel = viewModel()) {
    var showPopup by remember { mutableStateOf(false) }
    var selectedPetId by remember { mutableStateOf("") }
    val petDetails by mainViewModel.petDetails.collectAsState()
    val missingPets = remember { mutableStateOf<List<MissingPetDetails>>(emptyList()) }

    LaunchedEffect(petDetails) {
        val missingPetsVal = fetchMissingPets()
        missingPets.value = missingPetsVal
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Help other owners find their pets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Button(
                onClick = {
                    selectedPetId = petDetails?.petId ?: "Unknown" // Default petId
                    showPopup = true
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = "Lost My Pet")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable List of Missing Pets
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(missingPets.value) { pet ->
                MissingPetCard(pet)
            }
        }
    }

    if (showPopup) {
        ReportMissingPetPopup(
            petId = selectedPetId,
            onDismiss = { showPopup = false },
            onSave = { description ->
                saveMissingPetToFirestore(
                    petId = selectedPetId,
                    description = description,
                    onSuccess = {
                        println("Report saved successfully!")
                        showPopup = false
                    },
                    onFailure = { e ->
                        println("Failed to save report: ${e.message}")
                    }
                )
            },
            mainViewModel
        )
    }
}

fun saveMissingPetToFirestore(petId: String, description: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val missingPetData = mapOf(
        "petId" to petId,
        "description" to description,
        "lostDate" to Date() // Today's date
    )
    firestore.collection("missing")
        .add(missingPetData)
        .addOnSuccessListener {
            println("Missing pet report saved successfully")
            onSuccess()
        }
        .addOnFailureListener { e ->
            println("Failed to save missing pet report: ${e.message}")
            onFailure(e)
        }
}

data class MissingPetDetails(
    val petId: String,
    val petName: String,
    val petBreed: String,
    val petWeight: Int,
    val petColor: String,
    val petBirthday: Date,
    val petAdoptionDate: Date,
    val description: String,
    val lostDate: Date,
    val picture: String,
    val ownerName: String,
    val phoneNumber: String,
    val petType: String
)

suspend fun fetchMissingPets(): List<MissingPetDetails> {
    val firestore = FirebaseFirestore.getInstance()
    val missingPets = mutableListOf<MissingPetDetails>()

    try {
        // Fetch all documents from the 'missing' collection
        val missingSnapshot = firestore.collection("missing").get().await()

        for (missingDoc in missingSnapshot.documents) {
            val petId = missingDoc.getString("petId") ?: continue
            val description = missingDoc.getString("description") ?: "No description provided"
            val lostDate = missingDoc.getDate("lostDate") ?: continue

            // Fetch the corresponding pet details from the 'pets' collection
            val petDoc = firestore.collection("pets").document(petId).get().await()

            if (petDoc.exists()) {
                val petName = petDoc.getString("petName") ?: "Unknown"
                val petBreed = petDoc.getString("petBreed") ?: "Unknown"
                val petWeight = (petDoc.getLong("petWeight") ?: 0).toInt()
                val petColor = petDoc.getString("petColor") ?: "Unknown"
                val petBirthday = petDoc.getDate("petBirthday") ?: Date()
                val petAdoptionDate = petDoc.getDate("petAdoptionDate") ?: Date()
                val petPicture = petDoc.getString("picture") ?: "Unknown"
                val petType = petDoc.getString("petType") ?: "Unknown"

                // Fetch owner details from the 'users' collection
                val ownerId = petDoc.getString("ownerId") ?: continue
                val ownerDoc = firestore.collection("users").document(ownerId).get().await()

                val ownerName = ownerDoc.getString("name") ?: "Unknown"
                val ownerPhoneNumber = ownerDoc.getString("phone_number") ?: "Unknown"

                // Combine the data into the MissingPetDetails object
                val missingPetDetails = MissingPetDetails(
                    petId = petId,
                    petName = petName,
                    petBreed = petBreed,
                    petWeight = petWeight,
                    petColor = petColor,
                    petBirthday = petBirthday,
                    petAdoptionDate = petAdoptionDate,
                    description = description,
                    lostDate = lostDate,
                    ownerName = ownerName,
                    phoneNumber = ownerPhoneNumber,
                    picture = petPicture,
                    petType = petType
                )
                missingPets.add(missingPetDetails)
            }
        }
    } catch (e: Exception) {
        println("Error fetching missing pets: ${e.message}")
    }

    return missingPets
}
