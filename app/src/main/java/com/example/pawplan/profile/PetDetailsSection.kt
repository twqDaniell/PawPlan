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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pawplan.R
import com.example.pawplan.models.MainViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PetDetailsSection(mainViewModel: MainViewModel = viewModel()) {
    val userDetails by mainViewModel.userDetails.collectAsState()
    val petDetails by mainViewModel.petDetails.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet Image
        AsyncImage(
            model = petDetails?.picture ?: R.drawable.placeholder,
            contentDescription = "Pet Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pet Name
        Text(
            text = petDetails?.petName ?: "Loading...",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pet Details
        if (userDetails != null && petDetails != null) {
            Text(
                text = buildString {
                    appendLine(petDetails?.petBreed ?: "Breed: Loading...")
                    appendLine("Age - ${petDetails?.petBirthDate?.let { calculateAge(it) } ?: "Loading..."}")
                    appendLine("Weight - ${petDetails?.petWeight ?: "Loading..."}")
                    appendLine("Color - ${petDetails?.petColor ?: "Loading..."}")
                    appendLine("Birthday - ${petDetails?.petBirthDate?.let { formatTimestampToDate(it) } ?: "Loading..."}")
                    appendLine("Adoption Date - ${petDetails?.petAdoptionDate?.let { formatTimestampToDate(it) } ?: "Loading..."}")
                    appendLine("Owner's Name - ${userDetails?.userName}")
                    appendLine("Phone Number - ${userDetails?.phoneNumber}")
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Loading details...",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun calculateAge(birthDate: Date): Int {
    val birthCalendar = Calendar.getInstance().apply { time = birthDate }
    val todayCalendar = Calendar.getInstance()

    val years = todayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    val isBeforeBirthdayThisYear = todayCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)

    return if (isBeforeBirthdayThisYear) years - 1 else years
}

fun formatTimestampToDate(date: Date?, pattern: String = "dd/MM/yyyy"): String {
    return date?.let {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.format(it)
    } ?: "Unknown"
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