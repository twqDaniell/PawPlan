package com.example.pawplan.missing

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Date

@Composable
fun EditMissingPetPopup(
    petDetails: MissingPetDetails,
    onDismiss: () -> Unit,
    onSave: (MissingPetDetails) -> Unit
) {
    var description by remember { mutableStateOf(petDetails.description) }
    var lostDate by remember { mutableStateOf(petDetails.lostDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Missing Pet") },
        text = {
            Column {
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
                    val updatedDetails = petDetails.copy(
                        description = description,
                        lostDate = lostDate
                    )
                    onSave(updatedDetails)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
