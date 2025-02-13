package com.example.pawplan

import com.example.pawplan.models.VetVisit

interface VetVisitDao {
    fun insertVetVisit(visit: VetVisit, petId: String)
    fun getVetVisitsByPetId(petId: String): List<VetVisit>
    fun deleteVetVisit(id: String)
    fun clearVetVisits()
}
