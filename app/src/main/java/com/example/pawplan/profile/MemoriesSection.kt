package com.example.pawplan.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.pawplan.R

@Composable
fun MemoriesSection() {
    val imageUrls = listOf(
        "@drawable/logo",
        "@drawable/logo",
        "@drawable/logo",
        "@drawable/logo",
        "@drawable/logo",
        "@drawable/logo"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // Padding below the row
            horizontalArrangement = Arrangement.SpaceBetween, // Space between title and button
            verticalAlignment = Alignment.CenterVertically // Align items vertically
        ) {
            // Title
            Text(
                text = "Cheese's Memories",
                style = MaterialTheme.typography.headlineSmall
            )

            // Upload More Button
            OutlinedButton (
                onClick = { /* Upload More Logic */ },
            ) {
                Text("Upload More")
            }
        }


        // Grid of Images
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imageUrls) { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .build(),
                    contentDescription = "Memory Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RectangleShape) // Optional: Change shape if needed
                )
            }
        }
    }
}
