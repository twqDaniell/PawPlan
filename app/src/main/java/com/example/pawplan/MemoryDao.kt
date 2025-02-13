package com.example.pawplan

import com.example.pawplan.models.Memory

interface MemoryDao {
    fun insertMemory(memory: Memory)
    fun getMemoriesByPetId(petId: String): List<Memory>
    fun clearMemories()
}
