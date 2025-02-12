package com.example.pawplan.models

import java.util.*

data class MissingPet(
    val postId: String = "",
    val petId: String = "",
    val userId: String = "",
    val description: String = "",
    val lostDate: Date = Date(),
    val picture: String = ""
)
