package com.example.pawplan

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScrollContent(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.padding(innerPadding) // Apply Scaffold's padding
    ) {
        items(20) { index ->
            BasicText(
                text = "Item $index",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
