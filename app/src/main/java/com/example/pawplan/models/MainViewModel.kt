package com.example.pawplan.models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

data class UserDetails(
    val userName: String = "",
    val phoneNumber: String = ""
)

data class PetDetails(
    val petId: String = "",
    val petName: String = "",
    val petBreed: String = "",
    val petWeight: Int = 0,
    val petColor: String = "",
    val petBirthDate: Date,
    val petAdoptionDate: Date,
    val pictureUrl: String = ""
)

class MainViewModel : ViewModel() {
    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> get() = _userDetails

    private val _petDetails = MutableStateFlow<PetDetails?>(null)
    val petDetails: StateFlow<PetDetails?> get() = _petDetails

    init {
        fetchUserAndPetDetails()
    }

    fun fetchUserAndPetDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch user details
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "Unknown"
                    val phoneNumber = document.getString("phone_number") ?: "Unknown"
                    _userDetails.value = UserDetails(userName, phoneNumber)
                }
            }

        // Fetch pet details
        FirebaseFirestore.getInstance().collection("pets")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val firstPet = documents.documents.firstOrNull()
                if (firstPet != null) {
                    _petDetails.value = PetDetails(
                        petId = firstPet.id,
                        petName = firstPet.getString("petName") ?: "Unknown",
                        petBreed = firstPet.getString("petBreed") ?: "Unknown",
                        petWeight = (firstPet.getLong("petWeight") ?: 0).toInt(),
                        petColor = firstPet.getString("petColor") ?: "Unknown",
                        petBirthDate = firstPet.getDate("petBirthDate") ?: Date(0), // Fallback to epoch
                        petAdoptionDate = firstPet.getDate("petAdoptionDate") ?: Date(0),
                        pictureUrl = firstPet.getString("picture") ?: ""
                    )
                }
            }
    }
}
