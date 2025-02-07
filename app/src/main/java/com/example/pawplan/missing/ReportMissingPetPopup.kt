package com.example.pawplan.missing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.pawplan.models.MainViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun ReportMissingPetPopup(
    petId: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit, // Updated to include image URL
    mainViewModel: MainViewModel = viewModel()
) {
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // Image Picker Launcher
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Missing Pet") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Upload Section
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Text("Tap to upload photo", color = Color.Gray)
                    }
                }

                // Description Field
                Text("Tell people where and when you lost your pet", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (imageUri != null) {
                        isUploading = true
                        uploadImageToFirebase(imageUri!!) { imageUrl ->
                            uploadedImageUrl = imageUrl
                            onSave(description, uploadedImageUrl)
                            onDismiss()
                            isUploading = false
                        }
                    } else {
                        onSave(description, "")
                        onDismiss()
                    }
                },
                enabled = description.isNotBlank() && !isUploading
            ) {
                Text(if (isUploading) "Uploading..." else "Post")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Function to upload the image to Firebase Storage
fun uploadImageToFirebase(imageUri: Uri, onUploadSuccess: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileName = "missing_pets/${UUID.randomUUID()}.jpg"
    val fileRef = storageRef.child(fileName)

    fileRef.putFile(imageUri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                onUploadSuccess(uri.toString())
            }
        }
        .addOnFailureListener {
            println("Image upload failed: ${it.message}")
        }
}
