package com.example.pawplan.models

data class BreedsResponse(
    val message: Map<String, List<String>>, // Breeds and sub-breeds
    val status: String // Success status
)
