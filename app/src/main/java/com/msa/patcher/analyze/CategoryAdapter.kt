package com.msa.patcher.analyze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msa.patcher.R

class CategoryAdapter(
    private var items: List<AnalysisCategory>,
    private val onClick: (AnalysisCategory) -> Unit = {}
) : RecyclerView.Adapter<CategoryAdapter.Holder>() {
    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val number: TextView = v.findViewById(R.id.categoryNumber)
        val title: TextView = v.findViewById(R.id.categoryTitle)
        val state: TextView = v.findViewById(R.id.categoryState)
    }
    fun submitItems(newItems: List<AnalysisCategory>) { items = newItems; notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_analysis_category, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.number.text = item.number.toString().padStart(2, '0')
        holder.title.text = item.title
        holder.state.text = item.state
        holder.itemView.setOnClickListener { onClick(item) }
    }
}
