//package com.example.pawplan.missing
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.pawplan.models.MainViewModel
//
//@Composable
//fun MissingPetsScreenPreview(mainViewModel: MainViewModel = viewModel(), petId: String) {
//    val petDetails by mainViewModel.petDetails.collectAsState()
//
//    val samplePets = listOf(
//        PetPost(
//            name = "Rocky",
//            imageUrl = "https://via.placeholder.com/150",
//            lastSeenTime = "yesterday at 10:30",
//            lastSeenLocation = "Even Gvirol street in Tel Aviv",
//            ownerName = "Noa",
//            ownerContact = "0508818181"
//        ),
//        PetPost(
//            name = "Bella",
//            imageUrl = "https://via.placeholder.com/150",
//            lastSeenTime = "two days ago at 15:00",
//            lastSeenLocation = "Hayarkon Park",
//            ownerName = "Dan",
//            ownerContact = "0523456789"
//        )
//    )
//    MissingScreen(pets = samplePets, petDetails?.petId ?: "Unknown")
//}