package com.example.pawplan.missing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun EditMissingPetPopup(
    petDetails: MissingPetDetails,
    onDismiss: () -> Unit,
    onSave: (MissingPetDetails) -> Unit
) {
    var description by remember { mutableStateOf(petDetails.description) }
    var lostDate by remember { mutableStateOf(petDetails.lostDate) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf(petDetails.picture) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val isChanged = description != petDetails.description || imageUri != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Missing Pet") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Upload Section
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(250.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    } else {
                        Image(
                            painter = rememberImagePainter(uploadedImageUrl),
                            contentDescription = "Existing Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(250.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                }

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (imageUri != null) {
                        isUploading = true
                        imageUri?.let { uri ->
                            uploadImageToFirebase(uri) { imageUrl ->
                                uploadedImageUrl = imageUrl
                                val updatedDetails = petDetails.copy(
                                    description = description,
                                    lostDate = lostDate,
                                    picture = uploadedImageUrl
                                )
                                onSave(updatedDetails)
                                isUploading = false
                            }
                        }
                    } else {
                        val updatedDetails = petDetails.copy(
                            description = description,
                            lostDate = lostDate,
                            picture = uploadedImageUrl
                        )
                        onSave(updatedDetails)
                    }
                },
                enabled = isChanged && !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isChanged) MaterialTheme.colorScheme.primary else Color.Gray
                )
            ) {
                Text(if (isUploading) "Uploading..." else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
