package com.example.pawplan.missing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pawplan.R
import com.example.pawplan.models.MissingPet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MissingPetAdapter(
    private var allMissingPets: MutableList<MissingPet>,
    private val onEditClick: (MissingPet) -> Unit,
    private val onDeleteClick: (MissingPet) -> Unit
) : RecyclerView.Adapter<MissingPetAdapter.ViewHolder>() {

    private var displayedPets: MutableList<MissingPet> = allMissingPets.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_missing_pet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val missingPet = displayedPets[position]
        holder.bind(missingPet)
    }

    override fun getItemCount(): Int = displayedPets.size

    // ✅ Filter List Without Refetching
    fun filterList(petId: String?, showOnlyMyPosts: Boolean) {
        displayedPets = if (showOnlyMyPosts && petId != null) {
            allMissingPets.filter { it.petId == petId }.toMutableList()
        } else {
            allMissingPets.toMutableList() // Show all posts
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val petImage: ImageView = view.findViewById(R.id.missingPetImage)
        private val petName: TextView = view.findViewById(R.id.missingPetName)
        private val petDetails: TextView = view.findViewById(R.id.missingPetDetails)
        private val lostDate: TextView = view.findViewById(R.id.lostDate)
        private val petDescription: TextView = view.findViewById(R.id.missingPetDescription)
        private val editButton: ImageButton = view.findViewById(R.id.editPostButton)
        private val deleteButton: ImageButton = view.findViewById(R.id.deletePostButton)

        fun bind(missingPet: MissingPet) {
            petImage.load(missingPet.picture)
            lostDate.text = "Lost on: ${formatDate(missingPet.lostDate)}"
            petDescription.text = missingPet.description

            FirebaseFirestore.getInstance().collection("pets")
                .document(missingPet.petId)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("petName") ?: "Unknown Pet"
                    val breed = doc.getString("petBreed") ?: "Unknown Breed"
                    val color = doc.getString("petColor") ?: "Unknown Color"
                    petName.text = name
                    petDetails.text = "$color $breed"
                }

            if (missingPet.userId == FirebaseAuth.getInstance().currentUser?.uid) {
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
            } else {
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
            }

            editButton.setOnClickListener { onEditClick(missingPet) }
            deleteButton.setOnClickListener { onDeleteClick(missingPet) }
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}
