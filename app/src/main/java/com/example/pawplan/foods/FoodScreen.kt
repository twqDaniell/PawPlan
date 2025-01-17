package com.example.pawplan.foods

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pawplan.models.MainViewModel

import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FoodScreen(
    allergies: List<String>,
    mainViewModel: MainViewModel = viewModel()
) {
    val petDetails by mainViewModel.petDetails.collectAsState()
    var foodImageUrl = petDetails?.foodImage ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Help Text Section
        FoodHeaderSection(petDetails?.petName ?: "Unknown")

        // Food Image Section
        FoodImageSection(
            foodImageUrl = foodImageUrl,
            onImageUpload = { uri ->
                mainViewModel.viewModelScope.launch {
                    val petId = petDetails?.petId ?: "Unknown"
                    // Upload image to Firebase Storage
                    val uploadedUrl = uploadImageToFirebase(uri, petId)
                    if (uploadedUrl != null) {
                        // Save URL to Firestore or Realtime Database
                        saveImageUrlToFirestore(petId, uploadedUrl)
                        // Or use Realtime Database
                        // saveImageUrlToRealtimeDatabase(petId, uploadedUrl)

                        foodImageUrl = uploadedUrl
                        println("Uploaded and saved image URL: $uploadedUrl")
                    } else {
                        println("Image upload failed.")
                    }
                }
            }
        )

        // Allergies Section
        FoodAllergiesSection(petDetails?.petName ?: "Unknown", petDetails?.petId ?: "Unknown")
    }
}

suspend fun uploadImageToFirebase(uri: Uri, petId: String): String? {
    return try {
        // Get a reference to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        // Create a unique path for the image
        val fileRef = storageRef.child("pets/$petId/images/${uri.lastPathSegment}")

        // Upload the file
        val uploadTask = fileRef.putFile(uri).await()

        // Get the download URL
        val downloadUrl = fileRef.downloadUrl.await()
        downloadUrl.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun saveImageUrlToFirestore(petId: String, imageUrl: String) {
    val db = FirebaseFirestore.getInstance()
    try {
        // Save imageUrl under the pet's document
        db.collection("pets").document(petId)
            .update("foodImage", imageUrl) // Adds or updates the field "foodImage"
            .await()
        println("Image URL saved successfully.")
    } catch (e: Exception) {
        e.printStackTrace()
        println("Failed to save image URL to Firestore.")
    }
}