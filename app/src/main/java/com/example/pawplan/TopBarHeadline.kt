package com.example.pawplan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text("PawPlan")
        },
        navigationIcon = {
            val logo: Painter = painterResource(id = R.drawable.logo) // Replace with your logo resource
            Image(
                painter = logo,
                contentDescription = "App Logo",
                modifier = Modifier.size(40.dp)
            )
        }
    )
}