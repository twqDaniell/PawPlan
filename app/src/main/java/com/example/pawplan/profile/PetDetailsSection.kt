package com.example.pawplan.profile

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pawplan.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PetDetails(
    val name: String,
    val breed: String,
    val age: Int,
    val weight: Int,
    val color: String,
    val birthday: Date,
    val adoptionDate: Date,
    val ownerName: String,
    val phoneNumber: String,
    val pictureUrl: String
)

@Composable
fun PetDetailsSection() {
    val context = LocalContext.current
    var petDetails by remember { mutableStateOf<PetDetails?>(null) }
    var userName by remember { mutableStateOf("Loading...") }
    var userPhone by remember { mutableStateOf("Loading...") }

    // Fetch pet details from Firestore
    remember {
        fetchUserDetails(
            onUserDetailsFetched = { name, phone ->
                userName = name
                userPhone = phone
            },
            onError = { error ->
                Log.e("PetDetailsSection", error)
                userName = "Error"
                userPhone = "Error"
            }
        )

        FirebaseFirestore.getInstance()
            .collection("pets")
            .whereEqualTo("ownerId", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                val firstPet = documents.documents.firstOrNull()?.data
                if (firstPet != null) {
                    petDetails = PetDetails(
                        name = firstPet["petName"] as? String ?: "Unknown",
                        breed = firstPet["petBreed"] as? String ?: "Unknown",
                        age = (firstPet["petAge"] as? Long)?.toInt() ?: 0,
                        weight = (firstPet["petWeight"] as? Long)?.toInt() ?: 0,
                        color = firstPet["petColor"] as? String ?: "Unknown",
                        birthday = firstPet["petBirthDate"] as? Date ?: Date(),
                        adoptionDate = firstPet["petAdoptionDate"] as? Date ?: Date(),
                        ownerName = firstPet["ownerName"] as? String ?: "Unknown",
                        phoneNumber = firstPet["phoneNumber"] as? String ?: "Unknown",
                        pictureUrl = firstPet["picture"] as? String ?: ""
                    )
                } else {
                    Log.e("PetDetailsSection", "No pet details found.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PetDetailsSection", "Error fetching pet details: ${e.message}", e)
            }
    }

    // Show the pet details or a loading state
    if (petDetails != null) {
        PetDetailsContent(petDetails = petDetails!!, userName = userName, userPhone = userPhone)
    } else {
        Text("Loading pet details...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PetDetailsContent(petDetails: PetDetails, userName: String, userPhone: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(petDetails.pictureUrl)
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
        Text(text = petDetails.name, style = MaterialTheme.typography.headlineMedium)

        // Pet Details
        Text(
            text = """
                ${petDetails.breed}
                Age - ${calculateAge(petDetails.birthday)}
                Weight - ${petDetails.weight}
                Color - ${petDetails.color}
                Birthday - ${formatTimestampToDate(petDetails.birthday)}
                Adoption Date - ${formatTimestampToDate(petDetails.adoptionDate)}
                Owner's Name - ${userName}
                Phone Number - ${userPhone}
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

fun calculateAge(birthDate: Date): Int {
    val birthCalendar = Calendar.getInstance().apply { time = birthDate }
    val todayCalendar = Calendar.getInstance()

    val years = todayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    val isBeforeBirthdayThisYear = todayCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)

    return if (isBeforeBirthdayThisYear) years - 1 else years
}

fun formatTimestampToDate(date: Date, pattern: String = "dd/MM/yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault()) // Create a formatter
    return formatter.format(date) // Format the Date object
}

fun fetchUserDetails(onUserDetailsFetched: (String, String) -> Unit, onError: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId.isNullOrEmpty()) {
        onError("User ID is null or empty")
        return
    }

    FirebaseFirestore.getInstance().collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("name") ?: "Unknown Name"
                val phone = document.getString("phone_number") ?: "Unknown Phone"
                onUserDetailsFetched(name, phone)
            } else {
                onError("User document not found")
            }
        }
        .addOnFailureListener { e ->
            Log.e("FetchUserDetails", "Error fetching user details: ${e.message}", e)
            onError("Error fetching user details: ${e.message}")
        }
}