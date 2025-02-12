package com.example.pawplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.R
import com.example.pawplan.models.Allergy
import com.google.firebase.firestore.FirebaseFirestore

class AllergiesAdapter(
    private val allergiesList: MutableList<Allergy>
) : RecyclerView.Adapter<AllergiesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_allergy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val allergy = allergiesList[position]
        holder.bind(allergy)
    }

    override fun getItemCount(): Int = allergiesList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val allergyName: TextView = view.findViewById(R.id.allergyName)
        private val deleteButton: ImageButton = view.findViewById(R.id.deleteAllergyButton)

        fun bind(allergy: Allergy) {
            allergyName.text = allergy.allergyName

            deleteButton.setOnClickListener {
                deleteAllergy(allergy)
            }
        }

        private fun deleteAllergy(allergy: Allergy) {
            val db = FirebaseFirestore.getInstance()
            db.collection("allergies").document(allergy.id).delete()
                .addOnSuccessListener {
                    val position = allergiesList.indexOf(allergy)
                    if (position != -1) {
                        allergiesList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
        }
    }
}
