package com.example.pawplan.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaccinationRecordsSection(vetVisits: List<VetVisit>, onAddVisit: (VetVisit) -> Unit, petId: String) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title and Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vet Visits",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedButton(
                onClick = { showDialog = true  },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(36.dp) // Ensure consistent button size
            ) {
                Text("+", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Vaccination List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize() // Ensure it takes available space
        ) {
            items(vetVisits) { visit ->
                VaccinationRecordItem(
                    visit.topic,
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(visit.visitDate)
                )
            }
        }

        if (showDialog) {
            AddVisitPopup(
                onDismiss = { showDialog = false },
                onAdd = { newVisit ->
                    onAddVisit(newVisit)
                    showDialog = false
                },
                petId = petId
            )
        }
    }
}

@Composable
fun VaccinationRecordItem(title: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(text = date, style = MaterialTheme.typography.bodyMedium)
    }
}