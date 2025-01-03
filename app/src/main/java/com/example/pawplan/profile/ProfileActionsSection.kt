package com.example.pawplan.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileActionsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = { /* Edit Profile Logic */ }) {
            Text("Edit Profile")
        }
        Button(onClick = { /* Lost My Pet Logic */ }) {
            Text("Lost My Pet")
        }
    }
}
