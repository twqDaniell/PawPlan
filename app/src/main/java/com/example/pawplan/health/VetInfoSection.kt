package com.example.pawplan.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.example.pawplan.models.PetDetails
import com.example.pawplan.models.UserDetails
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VetDetails(
    val vetId: String = "",
    val vetName: String = "",
    val phoneNumber: String = ""
)

private val _vetDetails = MutableStateFlow<VetDetails?>(null)
val vetDetails: StateFlow<VetDetails?> get() = _vetDetails

@Composable
fun VetInfoSection(
    vetId: String,
    lastVisit: VetVisit,
    nextVisit: VetVisit,
    numberOfVisits: Int,
    weight: Int,
    petId: String
) {
    // State to control the visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }

    // Launch the fetch operation
    LaunchedEffect(vetId) {
        fetchVetDetails(vetId)
    }

    // Collect the vet details state
    val vetDetails by vetDetails.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp) // Space around the content
    ) {
        // Check if vetDetails is null (empty state)
        if (vetDetails == null) {
            AddVetButton(onClick = { showDialog = true }) // Show dialog when clicked
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // Space between the two columns
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Space between items
                    horizontalAlignment = Alignment.Start // Align text to the left
                ) {
                    // "Lilly's Vet" Title and Vet Name
                    Text(
                        text = "Lilly's Vet",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = vetDetails?.vetName ?: "Unknown name",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Last visited day
                    Text(
                        text = "Last visited day",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar Icon",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lastVisit.topic == "-") {
                                "-"
                            } else {
                                SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                                ).format(lastVisit.visitDate)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Last weight check
                    Text(
                        text = "Last weight check",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Weight Icon",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${weight}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Right Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Space between items
                    horizontalAlignment = Alignment.End // Align text to the right
                ) {
                    // Phone number
                    Text(
                        text = "Vet Phone",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vetDetails?.phoneNumber ?: "Unknown number",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Next Visit
                    Text(
                        text = "Next Visit",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar Icon",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (nextVisit.topic == "-") {
                                "-"
                            } else {
                                SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                                ).format(nextVisit.visitDate)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Total operations done
                    Text(
                        text = "Number of visits",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Operations Icon",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${numberOfVisits}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    // Show the Add Vet Dialog if showDialog is true
    if (showDialog) {
        showAddVetDialog(
            onDismiss = { showDialog = false },
            onAddVet = { vetName, phoneNumber ->
                saveVetToFirestore(vetName, phoneNumber, petId)
                showDialog = false
            }
        )
    }
}


fun fetchVetDetails(vetId: String) {
    FirebaseFirestore.getInstance().collection("vets").document(vetId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val vetName = document.getString("vetName") ?: "Unknown"
                val phoneNumber = document.getString("phoneNumber") ?: "Unknown"
                // Use the backing property (_vetDetails) to update the state
                _vetDetails.value = VetDetails(vetId, vetName, phoneNumber)
            } else {
                println("Vet document not found")
            }
        }
        .addOnFailureListener { exception ->
            println("Error fetching vet details: ${exception.message}")
        }
}

@Composable
fun showAddVetDialog(
    onDismiss: () -> Unit,
    onAddVet: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Check if the Add button should be enabled
    val isAddEnabled = name.isNotBlank() && phone.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vet") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vet Name") }
                )
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddVet(name, phone) },
                enabled = isAddEnabled // Disable Add button if condition is not met
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun saveVetToFirestore(
    vetName: String,
    phoneNumber: String,
    petId: String, // Add petId to update the corresponding pet
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val firestore = FirebaseFirestore.getInstance()

    // Create the vet data map
    val vetData = mapOf(
        "vetName" to vetName,
        "phoneNumber" to phoneNumber
    )

    // Add vet details to "vets" collection and get the generated vetId
    firestore.collection("vets")
        .add(vetData) // Automatically generates a document ID
        .addOnSuccessListener { documentReference ->
            val generatedVetId = documentReference.id // Get the auto-generated vetId
            println("Vet added with ID: $generatedVetId")

            // Update the pet document with the generated vetId
            firestore.collection("pets")
                .document(petId)
                .update("vetId", generatedVetId)
                .addOnSuccessListener {
                    println("Pet's vetId updated successfully")
                    onSuccess() // Trigger success callback
                    _vetDetails.value = VetDetails(
                        vetId = generatedVetId,
                        vetName = vetName,
                        phoneNumber = phoneNumber
                    )
                }
                .addOnFailureListener { e ->
                    println("Error updating pet's vetId: ${e.message}")
                    onFailure(e) // Trigger failure callback
                }
        }
        .addOnFailureListener { e ->
            println("Error adding vet: ${e.message}")
            onFailure(e) // Trigger failure callback
        }
}


