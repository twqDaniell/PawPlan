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
fun FoodAllergiesSection() {
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
                text = "Cheese's Food Allergies",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedButton(
                onClick = { /* Add allergy logic */ },
                modifier = Modifier.size(36.dp) // Ensure consistent button size
            ) {
                Text("+", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Allergy List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FoodAllergyItem("Artificial Additives")
            FoodAllergyItem("Beef")
            FoodAllergyItem("Soy")
        }
    }
}

@Composable
fun FoodAllergyItem(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}