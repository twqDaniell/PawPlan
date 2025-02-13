package com.example.pawplan

import com.example.pawplan.models.Pet

interface PetDao {
    fun insertPet(pet: Pet)
    fun getPet(): Pet?
    fun clearPets()
}
