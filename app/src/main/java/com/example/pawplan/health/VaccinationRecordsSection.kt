package com.example.pawplan.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VaccinationRecordsSection() {
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
                text = "Vaccination Records",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedButton(
                onClick = { /* Add vaccination logic */ },
                modifier = Modifier.size(36.dp) // Ensure consistent button size
            ) {
                Text("+", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Vaccination List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            VaccinationRecordItem("Rabies Vaccination", "01/03/2020")
            VaccinationRecordItem("Bordetella Vaccination", "30/05/2021")
            VaccinationRecordItem("Leptospirosis Vaccination", "11/12/2022")
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
