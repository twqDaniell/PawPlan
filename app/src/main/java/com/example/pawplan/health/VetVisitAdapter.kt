package com.example.pawplan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.R
import com.example.pawplan.models.VetVisit
import java.text.SimpleDateFormat
import java.util.*

class VetVisitAdapter(
    private val visitList: MutableList<VetVisit>,
    private val listener: VetVisitActionListener
) : RecyclerView.Adapter<VetVisitAdapter.VetVisitViewHolder>() {

    interface VetVisitActionListener {
        fun onDeleteVetVisit(visit: VetVisit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetVisitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vet_visit, parent, false)
        return VetVisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VetVisitViewHolder, position: Int) {
        val visit = visitList[position]
        holder.topicText.text = visit.topic
        holder.visitDateText.text = formatDate(visit.visitDate.toString())
        holder.deleteButton.setOnClickListener {
            listener.onDeleteVetVisit(visit)
        }
    }

    override fun getItemCount(): Int = visitList.size

    class VetVisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val topicText: TextView = itemView.findViewById(R.id.visitTopicText)
        val visitDateText: TextView = itemView.findViewById(R.id.visitDateText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteVetVisitButton)
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date: Date = inputFormat.parse(dateString) ?: return "-"
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }
}
