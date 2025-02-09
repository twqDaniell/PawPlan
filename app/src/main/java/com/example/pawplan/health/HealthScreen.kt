package com.example.pawplan.health

import FullPageLoader
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pawplan.models.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

data class VetVisit(
    val petId: String = "",
    val topic: String = "",
    val visitDate: Date = Date()
)

@Composable
fun HealthScreen(mainViewModel: MainViewModel) {
    val petDetails by mainViewModel.petDetails.collectAsState()
    val vetVisits = remember { mutableStateOf<List<VetVisit>>(emptyList()) }
    val loading by mainViewModel.loading.collectAsState()

    petDetails?.let { pet ->
        LaunchedEffect(pet.petId) {
            val visits = fetchVetVisitsForPet(pet.petId)
            vetVisits.value = visits
        }
    }

    FullPageLoader(isLoading = loading) {
        if (petDetails != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    HeaderSection(petDetails)
                    key(petDetails) {
                        VetInfoSection(
                            petDetails,
                            findLastVisit(vetVisits.value),
                            findNextVisit(vetVisits.value),
                            vetVisits.value.size,
                            mainViewModel
                        )
                    }

                    VaccinationRecordsSection(
                        vetVisits.value,
                        onAddVisit = { newVisit ->
                            vetVisits.value = vetVisits.value + newVisit },
                        petDetails?.petId ?: "Unknown"
                    )
                }
            }
        }
    }



}

suspend fun fetchVetVisitsForPet(petId: String): List<VetVisit> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("vetVisits")
            .whereEqualTo("petId", petId)
            .orderBy("visitDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await() // Wait for the query to complete

        // Map the documents to a list of VetVisit objects
        snapshot.documents.mapNotNull { document ->
            document.toObject(VetVisit::class.java)
        }
    } catch (e: Exception) {
        Log.e("sss","Error fetching vet visits: ${e.message}")
        emptyList()
    }
}

fun findLastVisit(vetVisits: List<VetVisit>): VetVisit {
    vetVisits.forEach { visit ->
        if(visit.visitDate.before(Date())) {
            return visit;
        }
    }

    return VetVisit("-", "-", Date())
}

fun findNextVisit(vetVisits: List<VetVisit>): VetVisit {
    val today = Calendar.getInstance()

    vetVisits.reversed().forEach { visit ->
        val targetDate = Calendar.getInstance()
        targetDate.time = visit.visitDate

        if(visit.visitDate.after(Date()) || today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR)) {
            return visit
        }
    }

    return VetVisit("-", "-", Date())
}