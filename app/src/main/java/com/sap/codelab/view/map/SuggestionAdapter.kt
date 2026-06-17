package com.sap.codelab.view.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sap.codelab.databinding.ItemSuggestionBinding
import com.sap.codelab.utils.location.NominatimPlace

internal class SuggestionAdapter(
    private val onSuggestionClick: (NominatimPlace) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    private val items = mutableListOf<NominatimPlace>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(suggestions: List<NominatimPlace>) {
        items.clear()
        items.addAll(suggestions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: NominatimPlace) {
            binding.suggestionText.text = place.displayName
            binding.root.setOnClickListener { onSuggestionClick(place) }
        }
    }
}