package com.example.pawplan.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pawplan.R

@Composable
fun PetDetailsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://your-image-url.com")
                .placeholder(R.drawable.logo) // Show this while loading
                .error(R.drawable.logo) // Show this on error
                .build(),
            contentDescription = "Pet Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pet Name
        Text(text = "Cheese", style = MaterialTheme.typography.headlineMedium)

        // Pet Details
        Text(
            text = """
                American Shorthair
                Age - 5
                Weight - 12
                Color - Ginger
                Birthday - 18.11.2012
                Adoption Date - 17.05.2017
                Owner's Name - Daniel
                Phone Number - 0507778855
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}