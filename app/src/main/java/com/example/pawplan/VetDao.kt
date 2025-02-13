package com.example.pawplan

import com.example.pawplan.models.Vet

interface VetDao {
    fun insertVet(vet: Vet)
    fun getVet(vetId: String): Vet?
    fun clearVets()
}
