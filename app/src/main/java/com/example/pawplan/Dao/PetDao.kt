package com.example.pawplan.Dao

import com.example.pawplan.models.Pet

interface PetDao {
    fun insertPet(pet: Pet)
    fun getPet(): Pet?
    fun clearPets()
}
