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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawplan.health.VetVisit
import com.example.pawplan.health.fetchVetVisitsForPet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

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

    LaunchedEffect(petId) {
        val petAllergies = fetchAllergiesForPet(petId)
        allergies.value = petAllergies
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
            IconButton(onClick = { /* Add allergy functionality */ }) {
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
}

suspend fun fetchAllergiesForPet(petId: String): List<Allergy> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("allergies")
            .whereEqualTo("petId", petId)
            .get()
            .await() // Wait for the query to complete

        // Map the documents to a list of VetVisit objects
        snapshot.documents.mapNotNull { document ->
            document.toObject(Allergy::class.java)
        }
    } catch (e: Exception) {
        Log.e("sss","Error fetching allergies: ${e.message}")
        emptyList()
    }
}