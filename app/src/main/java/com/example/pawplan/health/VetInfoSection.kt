package com.example.pawplan.health

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawplan.models.MainViewModel
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
    petDetails: PetDetails?,
    lastVisit: VetVisit,
    nextVisit: VetVisit,
    numberOfVisits: Int,
    mainViewModel: MainViewModel = viewModel()
) {
    val loading by mainViewModel.loading.collectAsState()
    if (loading) {
        CircularProgressIndicator()
    } else {
        // State to control the visibility of the Add/Edit dialog
        var showDialog by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }
        var editVetDetails by remember { mutableStateOf(VetDetails()) }

        // Collect the vet details state
        val vetDetails by vetDetails.collectAsState()

        // Fetch vet details when the vetId changes
        LaunchedEffect(petDetails?.vetId) {
            fetchVetDetails(petDetails?.vetId ?: "Unknown")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            if (vetDetails == null) {
                AddVetButton(onClick = {
                    isEditing = false
                    showDialog = true
                })
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column: Vet Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${petDetails?.petName}'s Vet",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = vetDetails?.vetName ?: "Unknown name",
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                                text = "${petDetails?.petWeight} kg",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Right Column: Additional Details and Edit Button
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
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
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
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
                                text = "$numberOfVisits",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // Edit Button
                        IconButton(
                            onClick =  {
                                isEditing = true
                                editVetDetails = vetDetails ?: VetDetails() // Pre-fill with current vet details
                                showDialog = true
                            },
                            modifier = Modifier.size(24.dp) // Set size for a compact look
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Post",
                                tint = MaterialTheme.colorScheme.primary // Red color for the delete icon
                            )
                        }
                    }
                }
            }
        }

        // Show Add/Edit Dialog
        if (showDialog) {
            showAddVetDialog(
                onDismiss = { showDialog = false },
                onAddVet = { vetName, phoneNumber ->
                    if (isEditing) {
                        // Update Firestore with new vet details
                        FirebaseFirestore.getInstance()
                            .collection("vets")
                            .document(editVetDetails.vetId)
                            .update(
                                mapOf(
                                    "vetName" to vetName,
                                    "phoneNumber" to phoneNumber
                                )
                            )
                            .addOnSuccessListener {
                                fetchVetDetails(editVetDetails.vetId) // Refresh details
                                showDialog = false
                            }
                            .addOnFailureListener { e ->
                                println("Error updating vet: ${e.message}")
                            }
                    } else {
                        // Add a new vet
                        saveVetToFirestore(
                            vetName = vetName,
                            phoneNumber = phoneNumber,
                            petId = petDetails?.petId ?: "",
                            onSuccess = {
                                fetchVetDetails(petDetails?.vetId ?: "")
                                showDialog = false
                            },
                            onFailure = { e ->
                                println("Error saving vet: ${e.message}")
                            }
                        )
                    }
                },
                defaultName = if (isEditing) editVetDetails.vetName else "",
                defaultPhone = if (isEditing) editVetDetails.phoneNumber else ""
            )
        }
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
    onAddVet: (String, String) -> Unit,
    defaultName: String = "", // Default name for editing
    defaultPhone: String = "" // Default phone for editing
) {
    var name by remember { mutableStateOf(defaultName) }
    var phone by remember { mutableStateOf(defaultPhone) }

    // Check if the Add button should be enabled
    val isAddEnabled = name.isNotBlank() && phone.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (defaultName.isNotEmpty()) "Edit Vet" else "Add Vet") },
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
                Text(if (defaultName.isNotEmpty()) "Save" else "Add")
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


