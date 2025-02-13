package com.example.pawplan.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.R
import com.squareup.picasso.Picasso

class MemoriesAdapter(private val memoryList: MutableList<String>) :
    RecyclerView.Adapter<MemoriesAdapter.MemoryViewHolder>() {

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memoryImageView: ImageView = itemView.findViewById(R.id.memory_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memoryUrl = memoryList[position]
        Picasso.get()
            .load(memoryUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.memoryImageView)
    }

    override fun getItemCount(): Int = memoryList.size
}
