package com.nammaplatform.app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ItemTrainBinding
import com.nammaplatform.app.models.TrainInfo

class TrainAdapter(
    private var trains: List<TrainInfo>,
    private val isCoachGuideMode: Boolean = false,
    private val onCoachClick: (TrainInfo) -> Unit,
    private val onSpeakClick: (TrainInfo) -> Unit,
    private val onDetailsClick: (TrainInfo) -> Unit
) : RecyclerView.Adapter<TrainAdapter.TrainViewHolder>() {

    private val TAG = "TrainAdapter"

    class TrainViewHolder(val binding: ItemTrainBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val binding = ItemTrainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val train = trains[position]
        val context = holder.binding.root.context
        
        Log.d(TAG, "Binding Train: #${train.number} | Departure: ${train.departureTime}")

        holder.binding.apply {
            tvTrainName.text = train.name
            tvTrainNumber.text = context.getString(R.string.train_no_prefix, train.number)
            
            tvArrival.text = train.arrivalTime.ifEmpty { "--:--" }
            tvPlatform.text = train.platform.ifEmpty { "TBD" }
            tvStatus.text = train.status.ifEmpty { "Scheduled" }
            
            // Ensure Departure is always visible as per requirement
            llDeparture.visibility = View.VISIBLE
            tvDeparture.text = train.departureTime.ifEmpty { "--:--" }
            
            if (!isCoachGuideMode) {
                tvDestination.visibility = View.VISIBLE
                tvDestination.text = context.getString(R.string.label_to, train.destination)
                btnDetails.visibility = View.VISIBLE
            } else {
                tvDestination.visibility = View.GONE
                btnDetails.visibility = View.GONE
            }

            val statusColor = if (train.status.contains("On Time", true)) {
                ContextCompat.getColor(context, R.color.status_on_time)
            } else {
                ContextCompat.getColor(context, R.color.status_delayed)
            }
            tvStatus.setTextColor(statusColor)

            btnViewCoach.setOnClickListener { onCoachClick(train) }
            btnSpeak.setOnClickListener { onSpeakClick(train) }
            btnDetails.setOnClickListener { onDetailsClick(train) }
            
            root.setOnClickListener { 
                if (isCoachGuideMode) onCoachClick(train) else onDetailsClick(train)
            }
        }
    }

    override fun getItemCount() = trains.size

    fun updateData(newTrains: List<TrainInfo>) {
        this.trains = newTrains
        notifyDataSetChanged()
    }
}
