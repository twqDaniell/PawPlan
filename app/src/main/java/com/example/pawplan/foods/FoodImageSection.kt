package com.example.pawplan.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun FoodImageSection(
    foodImageUrl: String?, // Nullable for cases where no image is uploaded
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { /* Add upload functionality */ },
        contentAlignment = Alignment.Center
    ) {
        if (foodImageUrl.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Default.Add, // Placeholder icon
                contentDescription = "Add Food Image",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
        } else {
            AsyncImage(
                model = foodImageUrl,
                contentDescription = "Food Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}