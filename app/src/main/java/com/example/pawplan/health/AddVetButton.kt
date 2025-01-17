package com.example.pawplan.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddVetButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Center text and button horizontally
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label above the button
        Text(
            text = "Add Vet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Circle button
        Button(
            onClick = onClick,
            shape = CircleShape, // Makes it circular
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(0.dp), // Remove extra padding
            modifier = Modifier
                .size(80.dp) // Size of the circle
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Vet",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp) // Size of the "+" icon
            )
        }
    }
}
