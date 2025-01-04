package com.example.pawplan.health

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddVisitPopup(onDismiss: () -> Unit, onAdd: (VetVisit) -> Unit, petId: String) {
    var topic by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf<Date?>(null) }
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Vet Visit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Topic Input Field
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Visit Topic") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker Button
                OutlinedButton (
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                visitDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = visitDate?.let { dateFormat.format(it) } ?: "Select Date"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (visitDate != null && topic.isNotBlank()) {
                        val visit = VetVisit(
                            petId = petId, // Replace with actual petId
                            topic = topic,
                            visitDate = visitDate!!
                        )
                        saveVetVisitToFirestore(
                            visit = visit,
                            onSuccess = {
                                println("Visit saved successfully")
                                onDismiss()
                            },
                            onFailure = { e ->
                                println("Failed to save visit: ${e.message}")
                            }
                        )
                        onAdd(visit)
                    }
                }
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

fun saveVetVisitToFirestore(visit: VetVisit, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("vetVisits")
        .add(visit)
        .addOnSuccessListener {
            println("Vet visit saved successfully")
            onSuccess()
        }
        .addOnFailureListener { e ->
            println("Failed to save vet visit: ${e.message}")
            onFailure(e)
        }
}