package com.msa.patcher.analyze

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msa.patcher.R

class AnalyzeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View = inflater.inflate(R.layout.fragment_analyze, container, false)
    override fun onViewCreated(view: View, state: Bundle?) {
        val list = view.findViewById<RecyclerView>(R.id.categoryList)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = CategoryAdapter(AnalysisPlan.ALL.mapIndexed { i, title -> AnalysisCategory(i + 1, title) })
    }
}
