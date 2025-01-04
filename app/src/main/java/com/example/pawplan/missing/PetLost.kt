package com.example.pawplan.missing

class PetPost(
    val name: String,
    val imageUrl: String,
    val lastSeenTime: String,
    val lastSeenLocation: String,
    val ownerName: String,
    val ownerContact: String
) {
    // Optionally add methods or additional functionality here
    fun getFormattedLastSeen(): String {
        return "Last seen $lastSeenTime on $lastSeenLocation"
    }

    fun getOwnerInfo(): String {
        return "Owner: $ownerName $ownerContact"
    }
}
