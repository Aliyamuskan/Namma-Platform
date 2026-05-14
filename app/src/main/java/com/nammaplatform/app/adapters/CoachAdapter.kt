package com.nammaplatform.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ItemCoachBinding
import com.nammaplatform.app.entities.CoachEntity

class CoachAdapter(private val coaches: List<CoachEntity>) : RecyclerView.Adapter<CoachAdapter.CoachViewHolder>() {

    class CoachViewHolder(val binding: ItemCoachBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoachViewHolder {
        val binding = ItemCoachBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoachViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoachViewHolder, position: Int) {
        val coach = coaches[position]
        holder.binding.apply {
            tvCoachName.text = coach.coachName
            tvCoachType.text = coach.coachType

            val context = root.context
            val color = when (coach.coachType.lowercase()) {
                "engine" -> R.color.coach_engine
                "general" -> R.color.coach_general
                "sleeper" -> R.color.coach_sleeper
                "ac" -> R.color.coach_ac
                "ladies" -> R.color.coach_ladies
                else -> R.color.railway_blue
            }
            llCoachBackground.setBackgroundColor(ContextCompat.getColor(context, color))
        }
    }

    override fun getItemCount() = coaches.size
}