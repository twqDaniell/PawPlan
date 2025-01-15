package com.example.pawplan.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pawplan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MemoriesSection(petId: String, petName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for memory image URLs
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch memories from Firestore
    LaunchedEffect(petId) {
        fetchMemories(petId) { urls ->
            imageUrls = urls
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                scope.launch(Dispatchers.IO) {
                    uploadMemoryImage(petId, uri) { downloadUrl ->
                        imageUrls = imageUrls + downloadUrl // Add new URL to the list
                    }
                }
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${petName}'s Memories",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Text(
                    text = "Upload More",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Grid of Images
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imageUrls) { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = "Memory Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RectangleShape)
                )
            }
        }
    }
}

// Fetch memories from Firestore
fun fetchMemories(petId: String, onResult: (List<String>) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("memories")
        .whereEqualTo("petId", petId)
        .get()
        .addOnSuccessListener { documents ->
            val urls = documents.mapNotNull { it.getString("picture") }
            onResult(urls)
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
            onResult(emptyList()) // Return empty list on failure
        }
}

// Upload memory image to Firebase Storage and save to Firestore
fun uploadMemoryImage(petId: String, imageUri: Uri, onSuccess: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileName = "memories/${UUID.randomUUID()}.jpg"
    val fileRef = storageRef.child(fileName)

    fileRef.putFile(imageUri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                saveMemoryToFirestore(petId, downloadUrl.toString(), onSuccess)
            }
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}

// Save memory to Firestore
fun saveMemoryToFirestore(petId: String, imageUrl: String, onSuccess: (String) -> Unit) {
    val memoryData = hashMapOf(
        "petId" to petId,
        "picture" to imageUrl
    )

    FirebaseFirestore.getInstance().collection("memories")
        .add(memoryData)
        .addOnSuccessListener {
            onSuccess(imageUrl)
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}
