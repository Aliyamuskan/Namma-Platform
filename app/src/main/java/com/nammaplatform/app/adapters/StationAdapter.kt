package com.nammaplatform.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nammaplatform.app.databinding.ItemStationBinding

class StationAdapter(
    private val stations: List<String>,
    private val onStationClick: (String) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    class StationViewHolder(val binding: ItemStationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val binding = ItemStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]
        holder.binding.tvStationName.text = station
        holder.binding.root.setOnClickListener { onStationClick(station) }
    }

    override fun getItemCount() = stations.size
}
