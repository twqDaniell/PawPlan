package com.example.pawplan.Dao

import com.example.pawplan.models.Allergy

interface AllergyDao {
    fun insertAllergy(allergy: Allergy, petId: String)
    fun getAllergiesByPetId(petId: String): List<Allergy>
    fun deleteAllergy(id: String)
    fun clearAllergies()
}

