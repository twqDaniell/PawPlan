package com.example.pawplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.AppDatabase
import com.example.pawplan.R
import com.example.pawplan.models.Allergy
import com.google.firebase.firestore.FirebaseFirestore

interface OnAllergyDeletedListener {
    fun onAllergyDeleted()
}

class AllergiesAdapter(
    private val allergiesList: MutableList<Allergy>,
    private val appDatabase: AppDatabase,
    private val deleteListener: OnAllergyDeletedListener
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val allergyName: TextView = itemView.findViewById(R.id.allergyName)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteAllergyButton)

        fun bind(allergy: Allergy) {
            allergyName.text = allergy.allergyName
            deleteButton.setOnClickListener {
                deleteAllergy(allergy)
            }
        }

        private fun deleteAllergy(allergy: Allergy) {
            FirebaseFirestore.getInstance().collection("allergies")
                .document(allergy.id)
                .delete()
                .addOnSuccessListener {
                    val position = allergiesList.indexOf(allergy)
                    if (position != -1) {
                        allergiesList.removeAt(position)
                        appDatabase.allergyDao.deleteAllergy(allergy.id)
                        notifyItemRemoved(position)
                        // Notify the fragment so it can update the title
                        deleteListener.onAllergyDeleted()
                    }
                }
        }
    }
}
