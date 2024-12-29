package com.example.pawplan

import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge

@Composable
fun BarsWithScaffold() {
    Scaffold(
        topBar = { SmallTopBar() }, // Reuse TopAppBar from another file
        bottomBar = { NavigationBarPawPlan() } // Reuse NavigationBar from another file
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(20) { index ->
                androidx.compose.material3.Text(text = "Item $index")
            }
        }
    }
}
