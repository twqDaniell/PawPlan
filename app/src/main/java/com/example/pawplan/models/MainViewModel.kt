package com.example.pawplan.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.pawplan.externalAPI.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class UserDetails(
    val userName: String = "",
    val phoneNumber: String = "",
    val _id: String = ""
)

data class PetDetails(
    val petId: String = "",
    val petName: String = "",
    val petBreed: String = "",
    val petWeight: Int = 0,
    val petColor: String = "",
    val petBirthDate: Date,
    val petAdoptionDate: Date,
    val picture: String = "",
    val vetId: String = "",
    val foodImage: String = "",
    val petType: String = "",
    val ownerId: String = ""
)

class MainViewModel : ViewModel() {
    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> get() = _userDetails

    private val _petDetails = MutableStateFlow<PetDetails?>(null)
    val petDetails: StateFlow<PetDetails?> get() = _petDetails

    private val _breeds = MutableStateFlow<List<String>>(emptyList())
    val breeds: StateFlow<List<String>> get() = _breeds

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> get() = _loading

    init {
        fetchUserAndPetDetails()
    }

    fun fetchUserAndPetDetails() {
        _loading.value = true
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

        FirebaseFirestore.getInstance().collection("pets")
            .whereEqualTo("ownerId", userId)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener { documents ->
                val firstPet = documents.documents.firstOrNull()
                if (firstPet != null) {
                    val fetchedWeight = (firstPet.getLong("petWeight") ?: 0).toInt()
//                    if (_petDetails.value?.petWeight != fetchedWeight) { // Only update if data is new
                        val updatedPetDetails = PetDetails(
                            petId = firstPet.id,
                            petName = firstPet.getString("petName") ?: "Unknown",
                            petBreed = firstPet.getString("petBreed") ?: "Unknown",
                            petWeight = fetchedWeight,
                            petColor = firstPet.getString("petColor") ?: "Unknown",
                            petBirthDate = firstPet.getDate("petBirthDate") ?: Date(0),
                            petAdoptionDate = firstPet.getDate("petAdoptionDate") ?: Date(0),
                            picture = firstPet.getString("picture") ?: "",
                            vetId = firstPet.getString("vetId") ?: "Unknown",
                            foodImage = firstPet.getString("foodImage") ?: "Unknown",
                            petType = firstPet.getString("petType") ?: "Unknown",
                            ownerId = firstPet.getString("ownerId") ?: "Unknown"
                        )
                        _petDetails.value = updatedPetDetails
                        Log.d("Firestore Fetch", "Updated petDetails: $updatedPetDetails")
//                    }
                }
                _loading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Fetch", "Error fetching pet details: ${e.message}")
                _loading.value = false
            }

    }

    fun updatePetDetails(updatedDetails: PetDetails) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("pets").document(updatedDetails.petId)
            .update(
                mapOf(
                    "petWeight" to updatedDetails.petWeight,
                    "petName" to updatedDetails.petName,
                    "petBreed" to updatedDetails.petBreed,
                    "petColor" to updatedDetails.petColor,
                    "petBirthDate" to updatedDetails.petBirthDate,
                    "petAdoptionDate" to updatedDetails.petAdoptionDate,
                    "picture" to updatedDetails.picture,
                    "vetId" to updatedDetails.vetId,
                    "foodImage" to updatedDetails.foodImage,
                    "petType" to updatedDetails.petType,
                    "ownerId" to updatedDetails.ownerId
                )
            )
            .addOnSuccessListener {
                _petDetails.value = updatedDetails.copy() // Update the state
                // Delay fetching Firestore data
//                kotlinx.coroutines.GlobalScope.launch {
//                    kotlinx.coroutines.delay(500) // Adjust delay based on Firestore sync latency
                    fetchUserAndPetDetails()
//                }

                Log.d("MainViewModel", petDetails.value?.petWeight.toString())
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Update", "Error updating details: ${e.message}")
            }
    }


    fun fetchBreeds(onBreedsFetched: (List<String>) -> Unit) {
        val api = RetrofitClient.instance
        api.getBreeds().enqueue(object : Callback<BreedsResponse> {
            override fun onResponse(call: Call<BreedsResponse>, response: Response<BreedsResponse>) {
                if (response.isSuccessful) {
                    val breedsMap = response.body()?.message ?: emptyMap()
                    val breedsList = breedsMap.keys.toList()
                    _breeds.value = breedsList
                    onBreedsFetched(breedsList)
                } else {
                    onBreedsFetched(emptyList())
                }
            }

            override fun onFailure(call: Call<BreedsResponse>, t: Throwable) {
                onBreedsFetched(emptyList())
            }
        })
    }
}
