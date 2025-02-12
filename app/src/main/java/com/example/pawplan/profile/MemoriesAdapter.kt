package com.example.pawplan.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.R
import coil.load

class MemoriesAdapter(private val memoryList: MutableList<String>) :
    RecyclerView.Adapter<MemoriesAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val imageUrl = memoryList[position]
        holder.memoryImageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }
    }

    override fun getItemCount(): Int = memoryList.size

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memoryImageView: ImageView = itemView.findViewById(R.id.memory_image)
    }
}
