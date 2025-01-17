import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pawplan.missing.MissingPetDetails
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MissingPetCard(
    pet: MissingPetDetails,
    isMyPost: Boolean, // Determines if the delete button should be visible
    onDelete: (MissingPetDetails) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        // Pet Image
        AsyncImage(
            model = pet.picture,
            contentDescription = "Image of ${pet.petName}",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Row for Pet Name and Delete Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Ensures spacing between name and button
        ) {
            // Pet Name
            Text(
                text = pet.petName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f) // Ensures the name takes available space
            )

            // Delete Button (Only visible if the user owns the post)
            if (isMyPost) {
                IconButton(
                    onClick = { onDelete(pet) },
                    modifier = Modifier.size(24.dp) // Set size for a compact look
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Post",
                        tint = MaterialTheme.colorScheme.error // Red color for the delete icon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Pet Details
        Text(
            text = pet.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${pet.petColor} ${pet.petBreed} ${pet.petType}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Owner Info
        Text(
            text = "${pet.ownerName} ${pet.phoneNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(pet.lostDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
