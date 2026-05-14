package com.nammaplatform.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nammaplatform.app.databinding.ItemFacilityBinding
import com.nammaplatform.app.models.FacilityModel

class FacilityAdapter(private val facilities: List<FacilityModel>) : RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder>() {

    class FacilityViewHolder(val binding: ItemFacilityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
        val binding = ItemFacilityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FacilityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
        val facility = facilities[position]
        holder.binding.apply {
            tvFacilityName.text = facility.name
            ivFacilityIcon.setImageResource(facility.icon)
        }
    }

    override fun getItemCount() = facilities.size
}