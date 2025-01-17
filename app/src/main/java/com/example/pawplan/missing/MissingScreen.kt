package com.example.pawplan.missing

import MissingPetCard
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawplan.models.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun MissingScreen(mainViewModel: MainViewModel) {
    var showPopup by remember { mutableStateOf(false) }
    var selectedPetId by remember { mutableStateOf("") }
    val petDetails by mainViewModel.petDetails.collectAsState()
    val userDetails by mainViewModel.userDetails.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var missingPets = remember { mutableStateOf<List<MissingPetDetails>>(emptyList()) }
    var showEditPopup by remember { mutableStateOf(false) }
    var petToEdit by remember { mutableStateOf<MissingPetDetails?>(null) }
    var showOnlyMyPosts by remember { mutableStateOf(false) }

    LaunchedEffect(petDetails) {
        var missingPetsVal = fetchMissingPets()
        missingPets.value = missingPetsVal
    }

    val filteredMissingPets = if (showOnlyMyPosts) {
        missingPets.value.filter { it.ownerId == userId } // Show only the user's posts
    } else {
        missingPets.value // Show all posts
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showOnlyMyPosts) "Showing My Posts" else "Showing All Posts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Switch(
                checked = showOnlyMyPosts,
                onCheckedChange = { showOnlyMyPosts = it }, // Update the state when toggled
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        val context = LocalContext.current

        // Scrollable List of Missing Pets
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
             // Get the context inside the composable scope

            items(filteredMissingPets) { pet ->
                MissingPetCard(
                    pet = pet,
                    isMyPost = pet.ownerId == userId,
                    onDelete = { petToDelete ->
                        deletePost(
                            postId = petToDelete.postId,
                            onSuccess = {
                                // Update the list to remove the deleted item
                                missingPets.value = missingPets.value.filter { it.postId != petToDelete.postId }
                                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                // Handle failure
                                Toast.makeText(
                                    context,
                                    "Failed to delete post: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    onEdit = { pet ->
                        petToEdit = pet // Assign the selected pet to petToEdit
                        showEditPopup = true // Show the edit popup
                    },
                )
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
                    onSuccess = { postId ->
                        println("Report saved successfully!")
                        missingPets.value += MissingPetDetails(
                                                petDetails?.petId ?: "Unknown",
                                        petDetails?.petName ?: "Unknown",
                                        petDetails?.petBreed ?: "Unknown",
                                        petDetails?.petWeight ?: 0,
                                petDetails?.petColor ?: "Unknown",
                            petDetails?.petBirthDate ?: Date(),
                            petDetails?.petAdoptionDate ?: Date(),
                            description,
                            Date(),
                            petDetails?.picture ?: "Unknown",
                                                userDetails?.userName ?: "Unknown",
                                                userDetails?.phoneNumber ?: "Unknown",
                                                petDetails?.petType ?: "Unknown",
                            userId.toString(),
                            postId
                        )
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

    if (showEditPopup && petToEdit != null) {
        EditMissingPetPopup(
            petDetails = petToEdit!!,
            onDismiss = { showEditPopup = false },
            onSave = { updatedDetails ->
                editPost(
                    postId = petToEdit!!.postId,
                    updatedDetails = updatedDetails,
                    onSuccess = {
                        // Update the local list
                        missingPets.value = missingPets.value.map {
                            if (it.postId == updatedDetails.postId) updatedDetails else it
                        }
                        showEditPopup = false
                    },
                    onFailure = { e ->
                    }
                )
            }
        )
    }
}

fun saveMissingPetToFirestore(petId: String, description: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val missingPetData = mapOf(
        "petId" to petId,
        "description" to description,
        "lostDate" to Date() // Today's date
    )
    firestore.collection("missing")
        .add(missingPetData)
        .addOnSuccessListener { documentReference ->
            val postId = documentReference.id
            println("Missing pet report saved successfully")
            onSuccess(postId)
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
    val petType: String,
    val ownerId: String,
    val postId: String
)

suspend fun fetchMissingPets(): List<MissingPetDetails> {
    val firestore = FirebaseFirestore.getInstance()
    val missingPets = mutableListOf<MissingPetDetails>()

    try {
        // Fetch all documents from the 'missing' collection
        val missingSnapshot = firestore.collection("missing").get().await()

        for (missingDoc in missingSnapshot.documents) {
            val postId = missingDoc.id
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
                    postId = postId,
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
                    petType = petType,
                    ownerId = ownerId
                )
                missingPets.add(missingPetDetails)
            }
        }
    } catch (e: Exception) {
        println("Error fetching missing pets: ${e.message}")
    }

    return missingPets
}

fun deletePost(
    postId: String, // Unique ID of the pet post
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("missing")
        .document(postId) // Assuming `petId` is the document ID in Firestore
        .delete()
        .addOnSuccessListener {
            Log.d("DeleteAction", "Post deleted successfully")
            onSuccess() // Notify success
        }
        .addOnFailureListener { exception ->
            Log.e("DeleteAction", "Error deleting post", exception)
            onFailure(exception) // Notify failure
        }
}

fun editPost(
    postId: String,
    updatedDetails: MissingPetDetails,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // Update the missing pet details in the 'missing' collection
    val updatedMissingData = mapOf(
        "description" to updatedDetails.description
    )

    db.collection("missing")
        .document(postId)
        .update(updatedMissingData)
        .addOnSuccessListener {
            Log.d("EditPost", "Missing post updated successfully")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("EditPost", "Error updating missing post", e)
            onFailure(e) // Notify failure
        }
}

